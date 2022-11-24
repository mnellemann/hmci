/*
 *    Copyright 2020 Mark Nellemann <mark.nellemann@gmail.com>
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package biz.nellemann.hmci;

import biz.nellemann.hmci.dto.toml.HmcConfiguration;
import biz.nellemann.hmci.dto.xml.Link;
import biz.nellemann.hmci.dto.xml.ManagementConsoleEntry;
import biz.nellemann.hmci.dto.xml.XmlFeed;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.Thread.sleep;

class ManagementConsole implements Runnable {

    private final static Logger log = LoggerFactory.getLogger(ManagementConsole.class);

    private final Integer refreshValue;
    private final Integer discoverValue;
    private final List<ManagedSystem> managedSystems = new ArrayList<>();


    private final RestClient restClient;
    private final InfluxClient influxClient;
    private final AtomicBoolean keepRunning = new AtomicBoolean(true);

    protected Integer responseErrors = 0;

    private Boolean doEnergy = true;
    private final List<String> excludeSystems;
    private final List<String> includeSystems;
    private final List<String> excludePartitions;
    private final List<String> includePartitions;


    ManagementConsole(HmcConfiguration configuration, InfluxClient influxClient) {
        this.refreshValue = configuration.refresh;
        this.discoverValue = configuration.discover;
        this.doEnergy = configuration.energy;
        this.influxClient = influxClient;
        restClient = new RestClient(configuration.url, configuration.username, configuration.password, configuration.trust);

        if(configuration.trace != null) {
            try {
                File traceDir = new File(configuration.trace);
                traceDir.mkdirs();
                if(traceDir.canWrite()) {
                    Boolean doTrace = true;
                } else {
                    log.warn("HmcInstance() - can't write to trace dir: " + traceDir.toString());
                }
            } catch (Exception e) {
                log.error("HmcInstance() - trace error: " + e.getMessage());
            }
        }
        this.excludeSystems = configuration.excludeSystems;
        this.includeSystems = configuration.includeSystems;
        this.excludePartitions = configuration.excludePartitions;
        this.includePartitions = configuration.includePartitions;
    }


    @Override
    public void run() {

        log.trace("run()");
        int executions = 0;

        restClient.login();
        discover();

        do {
            Instant instantStart = Instant.now();
            try {
                refresh();
                if (++executions > discoverValue) {   // FIXME: Change to time based logic
                    executions = 0;
                    discover();
                }
            } catch (Exception e) {
                log.error("run() - fatal error: {}", e.getMessage());
                keepRunning.set(false);
                throw new RuntimeException(e);
            }

            Instant instantEnd = Instant.now();
            long timeSpend = Duration.between(instantStart, instantEnd).toMillis();
            log.trace("run() - duration millis: " + timeSpend);
            if(timeSpend < (refreshValue * 1000)) {
                try {
                    long sleepTime = (refreshValue * 1000) - timeSpend;
                    log.trace("run() - sleeping millis: " + sleepTime);
                    if(sleepTime > 0) {
                        //noinspection BusyWait
                        sleep(sleepTime);
                    }
                } catch (InterruptedException e) {
                    log.error("run() - sleep interrupted", e);
                }
            } else {
                log.warn("run() - possible slow response from this HMC");
            }

        } while (keepRunning.get());


        // Logout of HMC
        restClient.logoff();

    }


    public void discover() {

        try {
            String xml = restClient.getRequest("/rest/api/uom/ManagementConsole");

            // Do not try to parse empty response
            if(xml == null || xml.length() <= 1) {
                responseErrors++;
                log.warn("discover() - no data.");
                return;
            }

            XmlMapper xmlMapper = new XmlMapper();
            XmlFeed xmlFeed = xmlMapper.readValue(xml, XmlFeed.class);
            ManagementConsoleEntry entry;

            if(xmlFeed.getEntry() == null){
                log.warn("discover() - xmlFeed.entry == null");
                return;
            }

            if(xmlFeed.getEntry().getContent().isManagementConsole()) {
                entry = xmlFeed.getEntry().getContent().getManagementConsole();
                log.info("discover() - {}", entry.getName());
            } else {
                throw new UnsupportedOperationException("Failed to deserialize ManagementConsole");
            }

            managedSystems.clear();
            for (Link link : entry.getAssociatedManagedSystems()) {
                ManagedSystem managedSystem = new ManagedSystem(restClient, link.getHref());
                managedSystem.setExcludePartitions(excludePartitions);
                managedSystem.setIncludePartitions(includePartitions);
                managedSystem.setDoEnergy(doEnergy);
                managedSystem.discover();

                // Only continue for powered-on operating systems
                if(managedSystem.entry != null && Objects.equals(managedSystem.entry.state, "operating")) {

                    // Check exclude / include
                    if (!excludeSystems.contains(managedSystem.name) && includeSystems.isEmpty()) {
                        managedSystems.add(managedSystem);
                        //log.info("discover() - adding !excluded system: {}", managedSystem.name);
                    } else if (!includeSystems.isEmpty() && includeSystems.contains(managedSystem.name)) {
                        managedSystems.add(managedSystem);
                        //log.info("discover() - adding included system: {}", managedSystem.name);
                    }
                }
            }

        } catch (Exception e) {
            log.warn("discover() - error: {}", e.getMessage());
        }

    }


    void refresh() {

        log.debug("refresh()");
        managedSystems.forEach( (system) -> {

            if(system.entry == null){
                log.warn("refresh() - system.entry == null");
                return;
            }

            system.refresh();

            influxClient.write(system.getDetails(), system.getTimestamp(),"server_details");
            influxClient.write(system.getMemoryMetrics(), system.getTimestamp(),"server_memory");
            influxClient.write(system.getProcessorMetrics(), system.getTimestamp(),"server_processor");
            influxClient.write(system.getPhysicalProcessorPool(), system.getTimestamp(),"server_physicalProcessorPool");
            influxClient.write(system.getSharedProcessorPools(), system.getTimestamp(),"server_sharedProcessorPool");

            if(doEnergy) {
                system.systemEnergy.refresh();
                if(system.systemEnergy.metric != null) {
                    influxClient.write(system.systemEnergy.getPowerMetrics(), system.getTimestamp(), "server_energy_power");
                    influxClient.write(system.systemEnergy.getThermalMetrics(), system.getTimestamp(), "server_energy_thermal");
                }
            }

            influxClient.write(system.getVioDetails(), system.getTimestamp(),"vios_details");
            influxClient.write(system.getVioProcessorMetrics(), system.getTimestamp(),"vios_processor");
            influxClient.write(system.getVioMemoryMetrics(), system.getTimestamp(),"vios_memory");
            influxClient.write(system.getVioNetworkLpars(), system.getTimestamp(),"vios_network_lpars");
            influxClient.write(system.getVioNetworkVirtualAdapters(), system.getTimestamp(),"vios_network_virtual");
            influxClient.write(system.getVioNetworkSharedAdapters(), system.getTimestamp(),"vios_network_shared");
            influxClient.write(system.getVioNetworkGenericAdapters(), system.getTimestamp(),"vios_network_generic");
            influxClient.write(system.getVioStorageLpars(), system.getTimestamp(),"vios_storage_lpars");
            influxClient.write(system.getVioStorageFiberChannelAdapters(), system.getTimestamp(),"vios_storage_FC");
            influxClient.write(system.getVioStorageVirtualAdapters(), system.getTimestamp(),"vios_storage_vFC");
            influxClient.write(system.getVioStoragePhysicalAdapters(), system.getTimestamp(),"vios_storage_physical");
            // Missing:  vios_storage_SSP

            system.logicalPartitions.forEach( (partition) -> {
                partition.refresh();
                influxClient.write(partition.getDetails(), partition.getTimestamp(),"lpar_details");
                influxClient.write(partition.getMemoryMetrics(), partition.getTimestamp(),"lpar_memory");
                influxClient.write(partition.getProcessorMetrics(), partition.getTimestamp(),"lpar_processor");
                influxClient.write(partition.getSriovLogicalPorts(), partition.getTimestamp(),"lpar_net_sriov");
                influxClient.write(partition.getVirtualEthernetAdapterMetrics(), partition.getTimestamp(),"lpar_net_virtual");
                influxClient.write(partition.getVirtualGenericAdapterMetrics(), partition.getTimestamp(),"lpar_storage_virtual");
                influxClient.write(partition.getVirtualFibreChannelAdapterMetrics(), partition.getTimestamp(),"lpar_storage_vFC");
            });

        });

    }


    /*
    private void writeTraceFile(String id, String json) {

        String fileName = String.format("%s-%s.json", id, Instant.now().toString());
        try {
            log.debug("Writing trace file: " + fileName);
            File traceFile = new File(traceDir, fileName);
            BufferedWriter writer = new BufferedWriter(new FileWriter(traceFile));
            writer.write(json);
            writer.close();
        } catch (IOException e) {
            log.warn("writeTraceFile() - " + e.getMessage());
        }
    }
    */

}
