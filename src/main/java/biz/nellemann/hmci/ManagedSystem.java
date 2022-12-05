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

import biz.nellemann.hmci.dto.xml.*;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

class ManagedSystem extends Resource {

    private final static Logger log = LoggerFactory.getLogger(ManagedSystem.class);

    protected final List<LogicalPartition> logicalPartitions = new ArrayList<>();
    protected final List<VirtualIOServer> virtualIOServers = new ArrayList<>();

    private List<String> excludePartitions = new ArrayList<>();
    private List<String> includePartitions = new ArrayList<>();

    private final RestClient restClient;
    private final InfluxClient influxClient;


    protected ManagedSystemEntry entry;

    protected ManagedSystemPcmPreference pcmPreference;
    protected SystemEnergy systemEnergy;

    protected boolean enableEnergyMonitoring = false;

    private String uriPath;
    public String name;
    public String id;


    public ManagedSystem(RestClient restClient, InfluxClient influxClient, String href) {
        log.debug("ManagedSystem() - {}", href);
        this.restClient = restClient;
        this.influxClient = influxClient;
        try {
            URI uri = new URI(href);
            uriPath = uri.getPath();
        } catch (URISyntaxException e) {
            log.error("ManagedSystem() - {}", e.getMessage());
        }
    }


    @Override
    public String toString() {
        //return String.format("[%s] %s (%s-%s %s)", id, name, type, model, serialNumber);
        return "TODO";
    }


    public void setExcludePartitions(List<String> excludePartitions) {
        this.excludePartitions = excludePartitions;
    }

    public void setIncludePartitions(List<String> includePartitions) {
        this.includePartitions = includePartitions;
    }

    public void setDoEnergy(Boolean enableEnergyMonitoring) {

        if(pcmPreference == null || !enableEnergyMonitoring) {
            return;
        }

        if(pcmPreference.energyMonitoringCapable && !pcmPreference.energyMonitorEnabled) {
            setPcmPreference();
        }

        systemEnergy = new SystemEnergy(restClient, influxClient, this);
    }


    public void discover() {

        try {
            String xml = restClient.getRequest(uriPath);

            // Do not try to parse empty response
            if(xml == null || xml.length() <= 1) {
                log.warn("discover() - no data.");
                return;
            }

            XmlMapper xmlMapper = new XmlMapper();
            XmlEntry xmlEntry = xmlMapper.readValue(xml, XmlEntry.class);

            if(xmlEntry.getContent() == null){
                log.warn("discover() - no content.");
                return;
            }

            this.id = xmlEntry.id;
            if(xmlEntry.getContent().isManagedSystem()) {
                entry = xmlEntry.getContent().getManagedSystemEntry();
                this.name = entry.getName();
                log.info("discover() - [{}] {} ({})", entry.machineTypeModelAndSerialNumber.getTypeAndModelAndSerialNumber(), entry.getName(), entry.systemFirmware);
            } else {
                throw new UnsupportedOperationException("Failed to deserialize ManagedSystem");
            }

            logicalPartitions.clear();
            for (Link link : this.entry.getAssociatedLogicalPartitions()) {
                LogicalPartition logicalPartition = new LogicalPartition(restClient, influxClient, link.getHref(), this);
                logicalPartition.discover();
                if(Objects.equals(logicalPartition.entry.partitionState, "running")) {
                    // Check exclude / include
                    if(!excludePartitions.contains(logicalPartition.name) && includePartitions.isEmpty()) {
                        logicalPartitions.add(logicalPartition);
                        //log.info("discover() - adding !excluded partition: {}", logicalPartition.name);
                    } else if(!includePartitions.isEmpty() && includePartitions.contains(logicalPartition.name)) {
                        logicalPartitions.add(logicalPartition);
                        //log.info("discover() - adding included partition: {}", logicalPartition.name);
                    }
                }
            }

            virtualIOServers.clear();
            for (Link link : this.entry.getAssociatedVirtualIOServers()) {
                VirtualIOServer virtualIOServer = new VirtualIOServer(restClient, link.getHref(), this);
                virtualIOServer.discover();
                virtualIOServers.add(virtualIOServer);
            }

        } catch (Exception e) {
            log.warn("discover() - error: {}", e.getMessage());
        }

    }


    public void refresh() {

        log.debug("refresh() - {}", name);
        try {
            String xml = restClient.getRequest(String.format("/rest/api/pcm/ManagedSystem/%s/ProcessedMetrics?NoOfSamples=%d", id, currentNumberOfSamples));

            // Do not try to parse empty response
            if(xml == null || xml.length() <= 1) {
                log.warn("refresh() - no data.");
                return;
            }

            XmlMapper xmlMapper = new XmlMapper();
            XmlFeed xmlFeed = xmlMapper.readValue(xml, XmlFeed.class);

            xmlFeed.entries.forEach((entry) -> {
                if (entry.category.term.equals("ManagedSystem")) {
                    Link link = entry.link;
                    if (link.getType() != null && Objects.equals(link.getType(), "application/json")) {
                        try {
                            URI jsonUri = URI.create(link.getHref());
                            String json = restClient.getRequest(jsonUri.getPath());
                            deserialize(json);
                        } catch (IOException e) {
                            log.error("refresh() - error 1: {}", e.getMessage());
                        }
                    }
                }
            });

            if(systemEnergy != null) {
                systemEnergy.refresh();
            }

            logicalPartitions.forEach(LogicalPartition::refresh);

        } catch (JsonParseException e) {
            log.warn("refresh() - parse error for: {}", name);
            metric = null;
        } catch (IOException e) {
            log.error("refresh() - error 2: {} {}", e.getClass(), e.getMessage());
            metric = null;
        }

    }


    @Override
    public void process(int sample) {

        log.debug("process() - {} - sample: {}", name, sample);

        influxClient.write(getDetails(sample),"server_details");
        influxClient.write(getMemoryMetrics(sample),"server_memory");
        influxClient.write(getProcessorMetrics(sample), "server_processor");
        influxClient.write(getPhysicalProcessorPool(sample),"server_physicalProcessorPool");
        influxClient.write(getSharedProcessorPools(sample),"server_sharedProcessorPool");
        if(systemEnergy != null) {
            systemEnergy.process();
        }

        influxClient.write(getVioDetails(sample),"vios_details");
        influxClient.write(getVioProcessorMetrics(sample),"vios_processor");
        influxClient.write(getVioMemoryMetrics(sample),"vios_memory");
        influxClient.write(getVioNetworkLpars(sample),"vios_network_lpars");
        influxClient.write(getVioNetworkVirtualAdapters(sample),"vios_network_virtual");
        influxClient.write(getVioNetworkSharedAdapters(sample),"vios_network_shared");
        influxClient.write(getVioNetworkGenericAdapters(sample),"vios_network_generic");
        influxClient.write(getVioStorageLpars(sample),"vios_storage_lpars");
        influxClient.write(getVioStorageFiberChannelAdapters(sample),"vios_storage_FC");
        influxClient.write(getVioStorageVirtualAdapters(sample),"vios_storage_vFC");
        influxClient.write(getVioStoragePhysicalAdapters(sample),"vios_storage_physical");
        // Missing:  vios_storage_SSP

        logicalPartitions.forEach(Resource::process);
    }


    public void setPcmPreference() {
        log.info("setPcmPreference()");

        try {
            String urlPath = String.format("/rest/api/pcm/ManagedSystem/%s/preferences", id);
            XmlMapper xmlMapper = new XmlMapper();

            if(pcmPreference.energyMonitoringCapable && !pcmPreference.energyMonitorEnabled) {
                log.warn("getPcmPreferences() - Enabling energyMonitor");
                pcmPreference.metadata.atom = null;
                pcmPreference.energyMonitorEnabled = true;
                //xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
                String updateXml = xmlMapper.writeValueAsString(pcmPreference);
                //log.warn(updateXml);
                restClient.postRequest(urlPath, updateXml);
            }
        } catch (IOException e) {
            pcmPreference.energyMonitorEnabled = false;
            log.warn("setPcmPreferences() - Error: {}", e.getMessage());
        }
    }


    public void getPcmPreferences() {

        log.debug("getPcmPreferences()");

        try {
            String urlPath = String.format("/rest/api/pcm/ManagedSystem/%s/preferences", id);
            String xml = restClient.getRequest(urlPath);

            // Do not try to parse empty response
            if(xml == null || xml.length() <= 1) {
                log.warn("getPcmPreferences() - no data.");
                return;
            }

            XmlMapper xmlMapper = new XmlMapper();
            XmlFeed xmlFeed = xmlMapper.readValue(xml, XmlFeed.class);

            if(xmlFeed.getEntry().getContent() == null){
                log.warn("getPcmPreferences() - no content.");
                log.info(xml);
                return;
            }

            if(xmlFeed.getEntry().getContent().isManagedSystemPcmPreference()) {
                pcmPreference = xmlFeed.getEntry().getContent().getManagedSystemPcmPreference();
                enableEnergyMonitoring = pcmPreference.energyMonitorEnabled;
            } else {
                throw new UnsupportedOperationException("Failed to deserialize ManagedSystemPcmPreference");
            }

        } catch (Exception e) {
            log.warn("getPcmPreferences() - Error: {}", e.getMessage());
        }
    }


    // System details
    List<Measurement> getDetails(int sample) {

        List<Measurement> list = new ArrayList<>();
        try {
            Map<String, String> tagsMap = new TreeMap<>();
            Map<String, Object> fieldsMap = new TreeMap<>();

            tagsMap.put("servername", entry.getName());
            log.trace("getDetails() - tags: " + tagsMap);

            fieldsMap.put("mtm", String.format("%s-%s %s",
                entry.getMachineTypeModelAndSerialNumber().getMachineType(),
                entry.getMachineTypeModelAndSerialNumber().getModel(),
                entry.getMachineTypeModelAndSerialNumber().getSerialNumber())
            );
            fieldsMap.put("APIversion", metric.getUtilInfo().version);
            fieldsMap.put("metric", metric.utilInfo.metricType);
            fieldsMap.put("frequency", metric.getUtilInfo().frequency);
            fieldsMap.put("nextract", "HMCi");
            fieldsMap.put("name", entry.getName());
            fieldsMap.put("utilizedProcUnits", metric.getSample(sample).systemFirmwareUtil.utilizedProcUnits);
            fieldsMap.put("assignedMem", metric.getSample(sample).systemFirmwareUtil.assignedMem);
            log.trace("getDetails() - fields: " + fieldsMap);


            list.add(new Measurement(getTimestamp(sample), tagsMap, fieldsMap));
        } catch (Exception e) {
            log.warn("getDetails() - error: {}", e.getMessage());
        }

        return list;
    }


    // System Memory
    List<Measurement> getMemoryMetrics(int sample) {

        List<Measurement> list = new ArrayList<>();
        try {
            HashMap<String, String> tagsMap = new HashMap<>();
            Map<String, Object> fieldsMap = new HashMap<>();

            tagsMap.put("servername", entry.getName());
            log.trace("getMemoryMetrics() - tags: " + tagsMap);

            fieldsMap.put("totalMem", metric.getSample(sample).serverUtil.memory.totalMem);
            fieldsMap.put("availableMem", metric.getSample(sample).serverUtil.memory.availableMem);
            fieldsMap.put("configurableMem", metric.getSample(sample).serverUtil.memory.configurableMem);
            fieldsMap.put("assignedMemToLpars", metric.getSample(sample).serverUtil.memory.assignedMemToLpars);
            fieldsMap.put("virtualPersistentMem", metric.getSample(sample).serverUtil.memory.virtualPersistentMem);
            log.trace("getMemoryMetrics() - fields: " + fieldsMap);

            list.add(new Measurement(getTimestamp(sample), tagsMap, fieldsMap));
        } catch (Exception e) {
            log.warn("getMemoryMetrics() - error: {}", e.getMessage());
        }

        return list;
    }


    // System Processor
    List<Measurement> getProcessorMetrics(int sample) {

        List<Measurement> list = new ArrayList<>();
        try {
            HashMap<String, String> tagsMap = new HashMap<>();
            HashMap<String, Object> fieldsMap = new HashMap<>();

            tagsMap.put("servername", entry.getName());
            log.trace("getProcessorMetrics() - tags: " + tagsMap);

            fieldsMap.put("totalProcUnits", metric.getSample(sample).serverUtil.processor.totalProcUnits);
            fieldsMap.put("utilizedProcUnits", metric.getSample(sample).serverUtil.processor.utilizedProcUnits);
            fieldsMap.put("availableProcUnits", metric.getSample(sample).serverUtil.processor.availableProcUnits);
            fieldsMap.put("configurableProcUnits", metric.getSample(sample).serverUtil.processor.configurableProcUnits);
            log.trace("getProcessorMetrics() - fields: " + fieldsMap);

            list.add(new Measurement(getTimestamp(sample), tagsMap, fieldsMap));
        } catch (Exception e) {
            log.warn("getProcessorMetrics() - error: {}", e.getMessage());
        }

        return list;
    }

    // Sytem Shared ProcessorPools
    List<Measurement> getSharedProcessorPools(int sample) {

        List<Measurement> list = new ArrayList<>();
        try {
            metric.getSample(sample).serverUtil.sharedProcessorPool.forEach(sharedProcessorPool -> {
                HashMap<String, String> tagsMap = new HashMap<>();
                HashMap<String, Object> fieldsMap = new HashMap<>();

                tagsMap.put("servername", entry.getName());
                tagsMap.put("pool", String.valueOf(sharedProcessorPool.id));
                tagsMap.put("poolname", sharedProcessorPool.name);
                log.trace("getSharedProcessorPools() - tags: " + tagsMap);

                fieldsMap.put("assignedProcUnits", sharedProcessorPool.assignedProcUnits);
                fieldsMap.put("availableProcUnits", sharedProcessorPool.availableProcUnits);
                fieldsMap.put("utilizedProcUnits", sharedProcessorPool.utilizedProcUnits);
                fieldsMap.put("borrowedProcUnits", sharedProcessorPool.borrowedProcUnits);
                fieldsMap.put("configuredProcUnits", sharedProcessorPool.configuredProcUnits);
                log.trace("getSharedProcessorPools() - fields: " + fieldsMap);

                list.add(new Measurement(getTimestamp(sample), tagsMap, fieldsMap));
            });
        } catch (Exception e) {
            log.warn("getSharedProcessorPools() - error: {}", e.getMessage());

        }

        return list;
    }

    // System Physical ProcessorPool
    List<Measurement> getPhysicalProcessorPool(int sample) {

        List<Measurement> list = new ArrayList<>();
        try {
            HashMap<String, String> tagsMap = new HashMap<>();
            HashMap<String, Object> fieldsMap = new HashMap<>();

            tagsMap.put("servername", entry.getName());
            log.trace("getPhysicalProcessorPool() - tags: " + tagsMap);

            fieldsMap.put("assignedProcUnits", metric.getSample(sample).serverUtil.physicalProcessorPool.assignedProcUnits);
            fieldsMap.put("availableProcUnits", metric.getSample(sample).serverUtil.physicalProcessorPool.availableProcUnits);
            fieldsMap.put("utilizedProcUnits", metric.getSample(sample).serverUtil.physicalProcessorPool.utilizedProcUnits);
            fieldsMap.put("configuredProcUnits", metric.getSample(sample).serverUtil.physicalProcessorPool.configuredProcUnits);
            fieldsMap.put("borrowedProcUnits", metric.getSample(sample).serverUtil.physicalProcessorPool.borrowedProcUnits);
            log.trace("getPhysicalProcessorPool() - fields: " + fieldsMap);

            list.add(new Measurement(getTimestamp(sample), tagsMap, fieldsMap));
        } catch (Exception e) {
            log.warn("getPhysicalProcessorPool() - error: {}", e.getMessage());
        }

        return list;
    }


    /**
     * VIO Aggregated Metrics are stored under the Managed System
     */


    // VIO Details
    List<Measurement> getVioDetails(int sample) {

        List<Measurement> list = new ArrayList<>();
        try {
            metric.getSample(sample).viosUtil.forEach(vio -> {

                HashMap<String, String> tagsMap = new HashMap<>();
                HashMap<String, Object> fieldsMap = new HashMap<>();

                tagsMap.put("servername", entry.getName());
                tagsMap.put("viosname", vio.name);
                log.trace("getVioDetails() - tags: " + tagsMap);

                fieldsMap.put("viosid", vio.id);
                fieldsMap.put("viosstate", vio.state);
                fieldsMap.put("viosname", vio.name);
                fieldsMap.put("affinityScore", vio.affinityScore);
                log.trace("getVioDetails() - fields: " + fieldsMap);

                list.add(new Measurement(getTimestamp(sample), tagsMap, fieldsMap));
            });
        } catch (Exception e) {
            log.warn("getVioDetails() - error: {}", e.getMessage());
        }

        return list;
    }


    // VIO Memory
    List<Measurement> getVioMemoryMetrics(int sample) {

        List<Measurement> list = new ArrayList<>();
        try {
            metric.getSample(sample).viosUtil.forEach(vio -> {

                HashMap<String, String> tagsMap = new HashMap<>();
                HashMap<String, Object> fieldsMap = new HashMap<>();

                tagsMap.put("servername", entry.getName());
                tagsMap.put("viosname", vio.name);
                log.trace("getVioMemoryMetrics() - tags: " + tagsMap);

                Number assignedMem = vio.memory.assignedMem;
                Number utilizedMem = vio.memory.utilizedMem;
                Number usedMemPct = (utilizedMem.intValue() * 100 ) / assignedMem.intValue();
                fieldsMap.put("assignedMem", vio.memory.assignedMem);
                fieldsMap.put("utilizedMem", vio.memory.utilizedMem);
                fieldsMap.put("utilizedPct", usedMemPct.floatValue());
                log.trace("getVioMemoryMetrics() - fields: " + fieldsMap);

                list.add(new Measurement(getTimestamp(sample), tagsMap, fieldsMap));
            });
        } catch (Exception e) {
            log.warn("getVioMemoryMetrics() - error: {}", e.getMessage());
        }

        return list;
    }


    // VIO Processor
    List<Measurement> getVioProcessorMetrics(int sample) {

        List<Measurement> list = new ArrayList<>();
        try {
            metric.getSample(sample).viosUtil.forEach(vio -> {

                HashMap<String, String> tagsMap = new HashMap<>();
                HashMap<String, Object> fieldsMap = new HashMap<>();

                tagsMap.put("servername", entry.getName());
                tagsMap.put("viosname", vio.name);
                log.trace("getVioProcessorMetrics() - tags: " + tagsMap);

                fieldsMap.put("utilizedProcUnits", vio.processor.utilizedProcUnits);
                fieldsMap.put("utilizedCappedProcUnits", vio.processor.utilizedCappedProcUnits);
                fieldsMap.put("utilizedUncappedProcUnits", vio.processor.utilizedUncappedProcUnits);
                fieldsMap.put("currentVirtualProcessors", vio.processor.currentVirtualProcessors);
                fieldsMap.put("maxVirtualProcessors", vio.processor.maxVirtualProcessors);
                fieldsMap.put("maxProcUnits", vio.processor.maxProcUnits);
                fieldsMap.put("entitledProcUnits", vio.processor.entitledProcUnits);
                fieldsMap.put("donatedProcUnits", vio.processor.donatedProcUnits);
                fieldsMap.put("idleProcUnits", vio.processor.idleProcUnits);
                fieldsMap.put("timeSpentWaitingForDispatch", vio.processor.timePerInstructionExecution);
                fieldsMap.put("timePerInstructionExecution", vio.processor.timeSpentWaitingForDispatch);
                fieldsMap.put("weight", vio.processor.weight);
                fieldsMap.put("mode", vio.processor.mode);
                log.trace("getVioProcessorMetrics() - fields: " + fieldsMap);

                list.add(new Measurement(getTimestamp(sample), tagsMap, fieldsMap));
            });
        } catch (Exception e) {
            log.warn("getVioProcessorMetrics() - error: {}", e.getMessage());
        }

        return list;
    }


    // VIOs - Network
    List<Measurement> getVioNetworkLpars(int sample) {

        List<Measurement> list = new ArrayList<>();
        try {
            metric.getSample(sample).viosUtil.forEach(vio -> {

                HashMap<String, String> tagsMap = new HashMap<>();
                HashMap<String, Object> fieldsMap = new HashMap<>();

                tagsMap.put("servername", entry.getName());
                tagsMap.put("viosname", vio.name);
                log.trace("getVioNetworkLpars() - tags: " + tagsMap);

                fieldsMap.put("clientlpars", vio.network.clientLpars.size());
                log.trace("getVioNetworkLpars() - fields: " + fieldsMap);

                list.add(new Measurement(getTimestamp(sample), tagsMap, fieldsMap));
            });

        } catch (Exception e) {
            log.warn("getVioNetworkLpars() - error: {}", e.getMessage());
        }

        return list;
    }


    // VIO Network - Shared
    List<Measurement> getVioNetworkSharedAdapters(int sample) {

        List<Measurement> list = new ArrayList<>();
        try {
            metric.getSample(sample).viosUtil.forEach(vio -> {
                vio.network.sharedAdapters.forEach(adapter -> {
                    HashMap<String, String> tagsMap = new HashMap<>();
                    HashMap<String, Object> fieldsMap = new HashMap<>();

                    tagsMap.put("servername", entry.getName());
                    tagsMap.put("viosname", vio.name);
                    //tagsMap.put("id", adapter.id);
                    tagsMap.put("location", adapter.physicalLocation);
                    log.trace("getVioNetworkSharedAdapters() - tags: " + tagsMap);

                    fieldsMap.put("id", adapter.id);
                    fieldsMap.put("type", adapter.type);
                    fieldsMap.put("sentBytes", adapter.sentBytes);
                    fieldsMap.put("sentPackets", adapter.sentPackets);
                    fieldsMap.put("receivedBytes", adapter.receivedBytes);
                    fieldsMap.put("receivedPackets", adapter.receivedPackets);
                    fieldsMap.put("droppedPackets", adapter.droppedPackets);
                    fieldsMap.put("transferredBytes", adapter.transferredBytes);
                    log.trace("getVioNetworkSharedAdapters() - fields: " + fieldsMap);

                    list.add(new Measurement(getTimestamp(sample), tagsMap, fieldsMap));
                });
            });
        } catch (Exception e) {
            log.warn("getVioNetworkSharedAdapters() - error: {}", e.getMessage());
        }

        return list;
    }


    // VIO Network - Virtual
    List<Measurement> getVioNetworkVirtualAdapters(int sample) {

        List<Measurement> list = new ArrayList<>();
        try {
            metric.getSample(sample).viosUtil.forEach( vio -> {
                vio.network.virtualEthernetAdapters.forEach( adapter -> {

                    HashMap<String, String> tagsMap = new HashMap<>();
                    HashMap<String, Object> fieldsMap = new HashMap<>();

                    tagsMap.put("vlanid", String.valueOf(adapter.vlanId));
                    tagsMap.put("vswitchid", String.valueOf(adapter.vswitchId));
                    tagsMap.put("servername", entry.getName());
                    tagsMap.put("viosname", vio.name);
                    tagsMap.put("location", adapter.physicalLocation);
                    log.trace("getVioNetworkVirtualAdapters() - tags: " + tagsMap);

                    fieldsMap.put("droppedPackets", adapter.droppedPackets);
                    fieldsMap.put("droppedPhysicalPackets", adapter.droppedPhysicalPackets);
                    fieldsMap.put("isPortVlanId", adapter.isPortVlanId);
                    fieldsMap.put("receivedBytes", adapter.receivedBytes);
                    fieldsMap.put("receivedPackets", adapter.receivedPackets);
                    fieldsMap.put("receivedPhysicalBytes", adapter.receivedPhysicalBytes);
                    fieldsMap.put("receivedPhysicalPackets", adapter.receivedPhysicalPackets);
                    fieldsMap.put("sentBytes", adapter.sentBytes);
                    fieldsMap.put("sentPackets", adapter.sentPackets);
                    fieldsMap.put("sentPhysicalBytes", adapter.sentPhysicalBytes);
                    fieldsMap.put("sentPhysicalPackets", adapter.sentPhysicalPackets);
                    fieldsMap.put("transferredBytes", adapter.transferredBytes);
                    fieldsMap.put("transferredPhysicalBytes", adapter.transferredPhysicalBytes);
                    log.trace("getVioNetworkVirtualAdapters() - fields: " + fieldsMap);

                    list.add(new Measurement(getTimestamp(sample), tagsMap, fieldsMap));
                });
            });
        } catch (Exception e) {
            log.warn("getVioNetworkVirtualAdapters() - error: {}", e.getMessage());
        }

        return list;
    }


    // VIO Network - Generic
    List<Measurement> getVioNetworkGenericAdapters(int sample) {

        List<Measurement> list = new ArrayList<>();
        try {
            metric.getSample(sample).viosUtil.forEach( vio -> {
                vio.network.genericAdapters.forEach( adapter -> {

                    HashMap<String, String> tagsMap = new HashMap<>();
                    HashMap<String, Object> fieldsMap = new HashMap<>();

                    tagsMap.put("id", adapter.id);
                    tagsMap.put("servername", entry.getName());
                    tagsMap.put("viosname", vio.name);
                    tagsMap.put("location", adapter.physicalLocation);
                    log.trace("getVioNetworkGenericAdapters() - tags: " + tagsMap);

                    fieldsMap.put("sentBytes", adapter.sentBytes);
                    fieldsMap.put("sentPackets", adapter.sentPackets);
                    fieldsMap.put("receivedBytes", adapter.receivedBytes);
                    fieldsMap.put("receivedPackets", adapter.receivedPackets);
                    fieldsMap.put("droppedPackets", adapter.droppedPackets);
                    fieldsMap.put("transferredBytes", adapter.transferredBytes);
                    log.trace("getVioNetworkGenericAdapters() - fields: " + fieldsMap);

                    list.add(new Measurement(getTimestamp(sample), tagsMap, fieldsMap));
                });
            });
        } catch (Exception e) {
            log.warn("getVioNetworkGenericAdapters() - error: {}", e.getMessage());
        }

        return list;
    }

    // VIOs - Storage
    List<Measurement> getVioStorageLpars(int sample) {

        List<Measurement> list = new ArrayList<>();
        try {
            metric.getSample(sample).viosUtil.forEach(vio -> {

                HashMap<String, String> tagsMap = new HashMap<>();
                HashMap<String, Object> fieldsMap = new HashMap<>();

                tagsMap.put("servername", entry.getName());
                tagsMap.put("viosname", vio.name);
                log.trace("getVioStorageLpars() - tags: " + tagsMap);

                fieldsMap.put("clientlpars", vio.storage.clientLpars.size());
                log.trace("getVioStorageLpars() - fields: " + fieldsMap);

                list.add(new Measurement(getTimestamp(sample), tagsMap, fieldsMap));
            });
        } catch (Exception e) {
            log.warn("getVioStorageLpars() - error: {}", e.getMessage());
        }

        return list;
    }

    // VIO Storage FC
    List<Measurement> getVioStorageFiberChannelAdapters(int sample) {

        List<Measurement> list = new ArrayList<>();
        try {
            metric.getSample(sample).viosUtil.forEach( vio -> {
                log.trace("getVioStorageFiberChannelAdapters() - VIO: " + vio.name);

                vio.storage.fiberChannelAdapters.forEach( adapter -> {

                    HashMap<String, String> tagsMap = new HashMap<>();
                    HashMap<String, Object> fieldsMap = new HashMap<>();

                    tagsMap.put("id", adapter.id);
                    tagsMap.put("servername", entry.getName());
                    tagsMap.put("viosname", vio.name);
                    tagsMap.put("location", adapter.physicalLocation);
                    log.trace("getVioStorageFiberChannelAdapters() - tags: " + tagsMap);

                    fieldsMap.put("numOfReads", adapter.numOfReads);
                    fieldsMap.put("numOfWrites", adapter.numOfWrites);
                    fieldsMap.put("readBytes", adapter.readBytes);
                    fieldsMap.put("writeBytes", adapter.writeBytes);
                    fieldsMap.put("transmittedBytes", adapter.transmittedBytes);
                    log.trace("getVioStorageFiberChannelAdapters() - fields: " + fieldsMap);

                    list.add(new Measurement(getTimestamp(sample), tagsMap, fieldsMap));
                });

            });

        } catch (Exception e) {
            log.warn("getVioStorageFiberChannelAdapters() - error: {}", e.getMessage());
        }

        return list;
    }


    // VIO Storage - Physical
    List<Measurement> getVioStoragePhysicalAdapters(int sample) {

        List<Measurement> list = new ArrayList<>();
        try {
            metric.getSample(sample).viosUtil.forEach( vio -> {
                log.trace("getVioStoragePhysicalAdapters() - VIO: " + vio.name);

                vio.storage.genericPhysicalAdapters.forEach( adapter -> {

                    HashMap<String, String> tagsMap = new HashMap<>();
                    HashMap<String, Object> fieldsMap = new HashMap<>();

                    tagsMap.put("servername", entry.getName());
                    tagsMap.put("viosname", vio.name);
                    tagsMap.put("id", adapter.id);
                    tagsMap.put("location", adapter.physicalLocation);
                    log.trace("getVioStoragePhysicalAdapters() - tags: " + tagsMap);

                    fieldsMap.put("numOfReads", adapter.numOfReads);
                    fieldsMap.put("numOfWrites", adapter.numOfWrites);
                    fieldsMap.put("readBytes", adapter.readBytes);
                    fieldsMap.put("writeBytes", adapter.writeBytes);
                    fieldsMap.put("transmittedBytes", adapter.transmittedBytes);
                    fieldsMap.put("type", adapter.type);
                    log.trace("getVioStoragePhysicalAdapters() - fields: " + fieldsMap);

                    list.add(new Measurement(getTimestamp(sample), tagsMap, fieldsMap));
                });
            });
        } catch (Exception e) {
            log.warn("getVioStoragePhysicalAdapters() - error: {}", e.getMessage());
        }

        return list;
    }


    // VIO Storage - Virtual
    List<Measurement> getVioStorageVirtualAdapters(int sample) {

        List<Measurement> list = new ArrayList<>();
        try {
            metric.getSample(sample).viosUtil.forEach( (vio) -> {
                vio.storage.genericVirtualAdapters.forEach( (adapter) -> {
                    HashMap<String, String> tagsMap = new HashMap<>();
                    HashMap<String, Object> fieldsMap = new HashMap<>();

                    tagsMap.put("servername", entry.getName());
                    tagsMap.put("viosname", vio.name);
                    tagsMap.put("location", adapter.physicalLocation);
                    tagsMap.put("id", adapter.id);
                    log.debug("getVioStorageVirtualAdapters() - tags: " + tagsMap);

                    fieldsMap.put("numOfReads", adapter.numOfReads);
                    fieldsMap.put("numOfWrites", adapter.numOfWrites);
                    fieldsMap.put("readBytes", adapter.readBytes);
                    fieldsMap.put("writeBytes", adapter.writeBytes);
                    fieldsMap.put("transmittedBytes", adapter.transmittedBytes);
                    fieldsMap.put("type", adapter.type);
                    log.debug("getVioStorageVirtualAdapters() - fields: " + fieldsMap);

                    list.add(new Measurement(getTimestamp(sample), tagsMap, fieldsMap));
                });
            });
        } catch (Exception e) {
            log.warn("getVioStorageVirtualAdapters() - error: {}", e.getMessage());
        }

        return list;
    }


    /*
    // VIO Storage SSP TODO
    List<Measurement> getViosStorageSharedStoragePools(int sample) {

        List<Measurement> list = new ArrayList<>();
        metrics.systemUtil.getSample(sample).viosUtil.forEach( vios -> {

            vios.storage.fiberChannelAdapters.forEach( adapter -> {

                HashMap<String, String> tagsMap = new HashMap<>();
                tagsMap.put("servername", name);
                tagsMap.put("viosname", vios.name);
                tagsMap.put("id", adapter.id);
                tagsMap.put("location", adapter.physicalLocation);
                log.trace("getViosStorageSharedStoragePools() - tags: " + tagsMap.toString());

                HashMap<String, Object> fieldsMap = new HashMap<>();
                fieldsMap.put("numOfReads", adapter.numOfReads);
                fieldsMap.put("numOfWrites", adapter.numOfWrites);
                fieldsMap.put("readBytes", adapter.readBytes);
                fieldsMap.put("writeBytes", adapter.writeBytes);
                fieldsMap.put("transmittedBytes", adapter.transmittedBytes);
                fieldsMap.put("physicalLocation", adapter.physicalLocation);
                log.trace("getViosStorageSharedStoragePools() - fields: " + fieldsMap.toString());

                list.add(new Measurement(getTimestamp(sample), tagsMap, fieldsMap));
            });

            log.trace("getViosStorageSharedStoragePools() - VIOS: " + vios.name);
        });

        return list;
    }
    */


}
