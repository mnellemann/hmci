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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializationFeature;
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

    protected ManagedSystemEntry entry;

    protected ManagedSystemPcmPreference pcmPreference;
    protected SystemEnergy systemEnergy;

    protected boolean enableEnergyMonitoring = false;

    private String uriPath;
    public String name;
    public String id;


    public ManagedSystem(RestClient restClient, String href) {
        log.debug("ManagedSystem() - {}", href);
        this.restClient = restClient;
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

    public void setDoEnergy(Boolean doEnergy) {

        if(pcmPreference == null) {
            return;
        }

        if(doEnergy && pcmPreference.energyMonitoringCapable && !pcmPreference.energyMonitorEnabled) {
            setPcmPreference();
        }

        if(pcmPreference.energyMonitorEnabled) {
            systemEnergy = new SystemEnergy(restClient, this);
        }

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
            } else {
                throw new UnsupportedOperationException("Failed to deserialize ManagedSystem");
            }

            logicalPartitions.clear();
            for (Link link : this.entry.getAssociatedLogicalPartitions()) {
                LogicalPartition logicalPartition = new LogicalPartition(restClient, link.getHref(), this);
                logicalPartition.discover();

                // Check exclude / include
                if(!excludePartitions.contains(logicalPartition.name) && includePartitions.isEmpty()) {
                    logicalPartitions.add(logicalPartition);
                    //log.info("discover() - adding !excluded partition: {}", logicalPartition.name);
                } else if(!includePartitions.isEmpty() && includePartitions.contains(logicalPartition.name)) {
                    logicalPartitions.add(logicalPartition);
                    //log.info("discover() - adding included partition: {}", logicalPartition.name);
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

        log.debug("refresh()");
        try {
            String xml = restClient.getRequest(String.format("/rest/api/pcm/ManagedSystem/%s/ProcessedMetrics?NoOfSamples=1", id));

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

        } catch (JsonParseException e) {
            log.warn("refresh() - parse error for: {}", name);
            metric = null;
        } catch (IOException e) {
            log.error("refresh() - error 2: {} {}", e.getClass(), e.getMessage());
            metric = null;
        }

    }

    public void setPcmPreference() {
        log.info("getPcmPreferences()");

        try {
            String urlPath = String.format("/rest/api/pcm/ManagedSystem/%s/preferences", id);
            XmlMapper xmlMapper = new XmlMapper();

            if(pcmPreference.energyMonitoringCapable && !pcmPreference.energyMonitorEnabled) {
                //log.warn("getPcmPreferences() - TODO: Enabling energyMonitor");
                pcmPreference.metadata.atom = null;
                pcmPreference.energyMonitorEnabled = true;
                //xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
                String updateXml = xmlMapper.writeValueAsString(pcmPreference);
                //log.warn(updateXml);
                restClient.postRequest(urlPath, updateXml);
            }
        } catch (IOException e) {
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
    List<Measurement> getDetails() {

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
            fieldsMap.put("utilizedProcUnits", metric.getSample().systemFirmwareUtil.utilizedProcUnits);
            fieldsMap.put("assignedMem", metric.getSample().systemFirmwareUtil.assignedMem);
            log.trace("getDetails() - fields: " + fieldsMap);

            list.add(new Measurement(tagsMap, fieldsMap));
        } catch (Exception e) {
            log.warn("getDetails() - error: {}", e.getMessage());
        }

        return list;
    }


    // System Memory
    List<Measurement> getMemoryMetrics() {

        List<Measurement> list = new ArrayList<>();

        try {
            HashMap<String, String> tagsMap = new HashMap<>();
            Map<String, Object> fieldsMap = new HashMap<>();

            tagsMap.put("servername", entry.getName());
            log.trace("getMemoryMetrics() - tags: " + tagsMap);

            fieldsMap.put("totalMem", metric.getSample().serverUtil.memory.totalMem);
            fieldsMap.put("availableMem", metric.getSample().serverUtil.memory.availableMem);
            fieldsMap.put("configurableMem", metric.getSample().serverUtil.memory.configurableMem);
            fieldsMap.put("assignedMemToLpars", metric.getSample().serverUtil.memory.assignedMemToLpars);
            fieldsMap.put("virtualPersistentMem", metric.getSample().serverUtil.memory.virtualPersistentMem);
            log.trace("getMemoryMetrics() - fields: " + fieldsMap);

            list.add(new Measurement(tagsMap, fieldsMap));
        } catch (Exception e) {
            log.warn("getMemoryMetrics() - error: {}", e.getMessage());
        }

        return list;
    }


    // System Processor
    List<Measurement> getProcessorMetrics() {

        List<Measurement> list = new ArrayList<>();

        try {
            HashMap<String, String> tagsMap = new HashMap<>();
            HashMap<String, Object> fieldsMap = new HashMap<>();

            tagsMap.put("servername", entry.getName());
            log.trace("getProcessorMetrics() - tags: " + tagsMap);

            fieldsMap.put("totalProcUnits", metric.getSample().serverUtil.processor.totalProcUnits);
            fieldsMap.put("utilizedProcUnits", metric.getSample().serverUtil.processor.utilizedProcUnits);
            fieldsMap.put("availableProcUnits", metric.getSample().serverUtil.processor.availableProcUnits);
            fieldsMap.put("configurableProcUnits", metric.getSample().serverUtil.processor.configurableProcUnits);
            log.trace("getProcessorMetrics() - fields: " + fieldsMap);

            list.add(new Measurement(tagsMap, fieldsMap));
        } catch (Exception e) {
            log.warn("getProcessorMetrics() - error: {}", e.getMessage());
        }

        return list;
    }

    // Sytem Shared ProcessorPools
    List<Measurement> getSharedProcessorPools() {

        List<Measurement> list = new ArrayList<>();
        try {

            metric.getSample().serverUtil.sharedProcessorPool.forEach(sharedProcessorPool -> {
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

                list.add(new Measurement(tagsMap, fieldsMap));
            });
        } catch (Exception e) {
            log.warn("getSharedProcessorPools() - error: {}", e.getMessage());

        }

        return list;
    }

    // System Physical ProcessorPool
    List<Measurement> getPhysicalProcessorPool() {

        List<Measurement> list = new ArrayList<>();

        try {
            HashMap<String, String> tagsMap = new HashMap<>();
            HashMap<String, Object> fieldsMap = new HashMap<>();

            tagsMap.put("servername", entry.getName());
            log.trace("getPhysicalProcessorPool() - tags: " + tagsMap);

            fieldsMap.put("assignedProcUnits", metric.getSample().serverUtil.physicalProcessorPool.assignedProcUnits);
            fieldsMap.put("availableProcUnits", metric.getSample().serverUtil.physicalProcessorPool.availableProcUnits);
            fieldsMap.put("utilizedProcUnits", metric.getSample().serverUtil.physicalProcessorPool.utilizedProcUnits);
            fieldsMap.put("configuredProcUnits", metric.getSample().serverUtil.physicalProcessorPool.configuredProcUnits);
            fieldsMap.put("borrowedProcUnits", metric.getSample().serverUtil.physicalProcessorPool.borrowedProcUnits);
            log.trace("getPhysicalProcessorPool() - fields: " + fieldsMap);

            list.add(new Measurement(tagsMap, fieldsMap));
        } catch (Exception e) {
            log.warn("getPhysicalProcessorPool() - error: {}", e.getMessage());
        }

        return list;
    }


    /**
     * VIO Aggregated Metrics are stored under the Managed System
     */


    // VIO Details
    List<Measurement> getVioDetails() {

        List<Measurement> list = new ArrayList<>();

        try {
            metric.getSample().viosUtil.forEach(vio -> {

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

                list.add(new Measurement(tagsMap, fieldsMap));
            });
        } catch (Exception e) {
            log.warn("getVioDetails() - error: {}", e.getMessage());
        }

        return list;
    }


    // VIO Memory
    List<Measurement> getVioMemoryMetrics() {

        List<Measurement> list = new ArrayList<>();
        try {
            metric.getSample().viosUtil.forEach(vio -> {

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

                list.add(new Measurement(tagsMap, fieldsMap));
            });
        } catch (Exception e) {
            log.warn("getVioMemoryMetrics() - error: {}", e.getMessage());
        }

        return list;
    }


    // VIO Processor
    List<Measurement> getVioProcessorMetrics() {

        List<Measurement> list = new ArrayList<>();
        try {
            metric.getSample().viosUtil.forEach(vio -> {

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

                list.add(new Measurement(tagsMap, fieldsMap));
            });
        } catch (Exception e) {
            log.warn("getVioProcessorMetrics() - error: {}", e.getMessage());
        }

        return list;
    }


    // VIOs - Network
    List<Measurement> getVioNetworkLpars() {

        List<Measurement> list = new ArrayList<>();
        try {
            metric.getSample().viosUtil.forEach(vio -> {

                HashMap<String, String> tagsMap = new HashMap<>();
                HashMap<String, Object> fieldsMap = new HashMap<>();

                tagsMap.put("servername", entry.getName());
                tagsMap.put("viosname", vio.name);
                log.trace("getVioNetworkLpars() - tags: " + tagsMap);

                fieldsMap.put("clientlpars", vio.network.clientLpars.size());
                log.trace("getVioNetworkLpars() - fields: " + fieldsMap);

                list.add(new Measurement(tagsMap, fieldsMap));
            });

        } catch (Exception e) {
            log.warn("getVioNetworkLpars() - error: {}", e.getMessage());
        }

        return list;
    }


    // VIO Network - Shared
    List<Measurement> getVioNetworkSharedAdapters() {

        List<Measurement> list = new ArrayList<>();
        try {
            metric.getSample().viosUtil.forEach(vio -> {
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

                    list.add(new Measurement(tagsMap, fieldsMap));
                });
            });
        } catch (Exception e) {
            log.warn("getVioNetworkSharedAdapters() - error: {}", e.getMessage());
        }

        return list;
    }


    // VIO Network - Virtual
    List<Measurement> getVioNetworkVirtualAdapters() {

        List<Measurement> list = new ArrayList<>();

        try {
            metric.getSample().viosUtil.forEach( vio -> {
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

                    list.add(new Measurement(tagsMap, fieldsMap));
                });
            });
        } catch (Exception e) {
            log.warn("getVioNetworkVirtualAdapters() - error: {}", e.getMessage());
        }

        return list;
    }


    // VIO Network - Generic
    List<Measurement> getVioNetworkGenericAdapters() {

        List<Measurement> list = new ArrayList<>();
        try {
            metric.getSample().viosUtil.forEach( vio -> {
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

                    list.add(new Measurement(tagsMap, fieldsMap));
                });
            });
        } catch (Exception e) {
            log.warn("getVioNetworkGenericAdapters() - error: {}", e.getMessage());
        }

        return list;
    }

    // VIOs - Storage
    List<Measurement> getVioStorageLpars() {

        List<Measurement> list = new ArrayList<>();
        try {
            metric.getSample().viosUtil.forEach(vio -> {

                HashMap<String, String> tagsMap = new HashMap<>();
                HashMap<String, Object> fieldsMap = new HashMap<>();

                tagsMap.put("servername", entry.getName());
                tagsMap.put("viosname", vio.name);
                log.trace("getVioStorageLpars() - tags: " + tagsMap);

                fieldsMap.put("clientlpars", vio.storage.clientLpars.size());
                log.trace("getVioStorageLpars() - fields: " + fieldsMap);

                list.add(new Measurement(tagsMap, fieldsMap));
            });
        } catch (Exception e) {
            log.warn("getVioStorageLpars() - error: {}", e.getMessage());
        }

        return list;
    }

    // VIO Storage FC
    List<Measurement> getVioStorageFiberChannelAdapters() {

        List<Measurement> list = new ArrayList<>();
        try {
            metric.getSample().viosUtil.forEach( vio -> {
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

                    list.add(new Measurement(tagsMap, fieldsMap));
                });

            });

        } catch (Exception e) {
            log.warn("getVioStorageFiberChannelAdapters() - error: {}", e.getMessage());
        }

        return list;
    }


    // VIO Storage - Physical
    List<Measurement> getVioStoragePhysicalAdapters() {

        List<Measurement> list = new ArrayList<>();
        try {
            metric.getSample().viosUtil.forEach( vio -> {
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

                    list.add(new Measurement(tagsMap, fieldsMap));
                });
            });
        } catch (Exception e) {
            log.warn("getVioStoragePhysicalAdapters() - error: {}", e.getMessage());
        }

        return list;
    }


    // VIO Storage - Virtual
    List<Measurement> getVioStorageVirtualAdapters() {

        List<Measurement> list = new ArrayList<>();
        try {
            metric.getSample().viosUtil.forEach( (vio) -> {
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

                    list.add(new Measurement(tagsMap, fieldsMap));
                });
            });
        } catch (Exception e) {
            log.warn("getVioStorageVirtualAdapters() - error: {}", e.getMessage());
        }

        return list;
    }


    /*
    // VIO Storage SSP TODO
    List<Measurement> getViosStorageSharedStoragePools() {

        List<Measurement> list = new ArrayList<>();
        metrics.systemUtil.sample.viosUtil.forEach( vios -> {

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

                list.add(new Measurement(tagsMap, fieldsMap));
            });

            log.trace("getViosStorageSharedStoragePools() - VIOS: " + vios.name);
        });

        return list;
    }
    */


}
