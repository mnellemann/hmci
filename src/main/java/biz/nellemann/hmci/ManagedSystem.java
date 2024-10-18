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

    private final ManagementConsole managementConsole;

    protected ManagedSystemEntry entry;

    protected ManagedSystemPcmPreference pcmPreference;
    protected SystemEnergy systemEnergy;

    protected boolean enableEnergyMonitoring = false;

    private String uriPath;
    public String name;
    public String id;


    public ManagedSystem(ManagementConsole managementConsole, String href) {
        log.debug("ManagedSystem() - {}", href);
        this.managementConsole = managementConsole;
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

        systemEnergy = new SystemEnergy(managementConsole, this);
    }


    public void discover() {

        try {
            String xml = managementConsole.getRestClient().getRequest(uriPath);

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
                LogicalPartition logicalPartition = new LogicalPartition(managementConsole, this, link.getHref());
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
                VirtualIOServer virtualIOServer = new VirtualIOServer(managementConsole, this, link.getHref());
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
            String xml = managementConsole.getRestClient().getRequest(String.format("/rest/api/pcm/ManagedSystem/%s/ProcessedMetrics?NoOfSamples=%d", id, noOfSamples));

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
                            String json = managementConsole.getRestClient().getRequest(jsonUri.getPath());
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
    public void process(int sample) throws NullPointerException {

        log.debug("process() - {} - sample: {}", name, sample);

        //managementConsole.writeMetric(getDetails(sample));
        managementConsole.writeMetric(getMemoryMetrics(sample));
        managementConsole.writeMetric(getProcessorMetrics(sample));
        //managementConsole.writeMetric(getPhysicalProcessorPool(sample));
        //managementConsole.getInfluxClient().write(getProcessorMetrics(sample), "server_processor");
        //managementConsole.getInfluxClient().write(getPhysicalProcessorPool(sample),"server_physicalProcessorPool");
        //managementConsole.getInfluxClient().write(getSharedProcessorPools(sample),"server_sharedProcessorPool");
        if(systemEnergy != null) {
            systemEnergy.process();
        }

        //managementConsole.getInfluxClient().write(getVioDetails(sample),"vios_details");
        //managementConsole.getInfluxClient().write(getVioProcessorMetrics(sample),"vios_processor");
        //managementConsole.getInfluxClient().write(getVioMemoryMetrics(sample),"vios_memory");
        //managementConsole.getInfluxClient().write(getVioNetworkLpars(sample),"vios_network_lpars");
        //managementConsole.getInfluxClient().write(getVioNetworkVirtualAdapters(sample),"vios_network_virtual");
        //managementConsole.getInfluxClient().write(getVioNetworkSharedAdapters(sample),"vios_network_shared");
        managementConsole.writeMetric(getVioNetworkSharedAdapters(sample));
        //managementConsole.getInfluxClient().write(getVioNetworkGenericAdapters(sample),"vios_network_generic");
        //managementConsole.getInfluxClient().write(getVioStorageLpars(sample),"vios_storage_lpars");
        //managementConsole.getInfluxClient().write(getVioStorageFiberChannelAdapters(sample),"vios_storage_FC");
        //managementConsole.getInfluxClient().write(getVioStorageVirtualAdapters(sample),"vios_storage_virtual");
        //managementConsole.getInfluxClient().write(getVioStoragePhysicalAdapters(sample),"vios_storage_physical");
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
                managementConsole.getRestClient().postRequest(urlPath, updateXml);
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
            String xml = managementConsole.getRestClient().getRequest(urlPath);

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
    List<MeasurementBundle> getDetails(int sample) throws NullPointerException {
        log.debug("getDetails()");
        List<MeasurementBundle> list = new ArrayList<>();

        Map<String, String> tags = new TreeMap<>();
        Map<String, Object> fields = new TreeMap<>();
        List<MeasurementItem> items = new ArrayList<>();

        tags.put("system", entry.getName());
        log.trace("getDetails() - tags: " + tags);

        String mtm = String.format("%s-%s %s",
            entry.getMachineTypeModelAndSerialNumber().getMachineType(),
            entry.getMachineTypeModelAndSerialNumber().getModel(),
            entry.getMachineTypeModelAndSerialNumber().getSerialNumber());
        fields.put("mtm", mtm);
        items.add(new MeasurementItem(MeasurementType.INFO, "mtm", mtm));

        fields.put("api_version", metric.getUtilInfo().version);
        items.add(new MeasurementItem(MeasurementType.INFO, "api_version",
            metric.getUtilInfo().version));

        fields.put("metric", metric.utilInfo.metricType);
        items.add(new MeasurementItem(MeasurementType.INFO, "metric",
            metric.getUtilInfo().metricType));

        fields.put("frequency", metric.getUtilInfo().frequency);
        items.add(new MeasurementItem(MeasurementType.INFO, "frequency",
            metric.getUtilInfo().frequency));

        fields.put("name", entry.getName());
        items.add(new MeasurementItem(MeasurementType.INFO, "name", entry.getName()));

        fields.put("utilized_proc_units", metric.getSample(sample).systemFirmwareUtil.utilizedProcUnits);
        items.add(new MeasurementItem(MeasurementType.GAUGE, "utilized_proc_units",
            metric.getSample(sample).systemFirmwareUtil.utilizedProcUnits));

        fields.put("assigned_mem", metric.getSample(sample).systemFirmwareUtil.assignedMem);
        items.add(new MeasurementItem(MeasurementType.GAUGE, "assigned_mem",
            metric.getSample(sample).systemFirmwareUtil.assignedMem));

        log.trace("getDetails() - fields: " + fields);

        list.add(new MeasurementBundle(getTimestamp(sample), "system_details", tags, fields, items));

        return list;
    }



    // System Memory
    List<MeasurementBundle> getMemoryMetrics(int sample) throws NullPointerException {
        log.debug("getMemoryMetrics()");

        List<MeasurementBundle> list = new ArrayList<>();

        HashMap<String, String> tags = new HashMap<>();
        Map<String, Object> fields = new HashMap<>();
        List<MeasurementItem> items = new ArrayList<>();

        tags.put("system", entry.getName());
        log.trace("getMemoryMetrics() - tags: " + tags);

        fields.put("installed", metric.getSample(sample).serverUtil.memory.totalMem);
        items.add(new MeasurementItem(MeasurementType.GAUGE, MeasurementUnit.MEGABYTES, "installed",
            metric.getSample(sample).serverUtil.memory.totalMem, "Memory installed in system"));

        fields.put("available", metric.getSample(sample).serverUtil.memory.availableMem);
        items.add(new MeasurementItem(MeasurementType.GAUGE, MeasurementUnit.MEGABYTES, "available",
            metric.getSample(sample).serverUtil.memory.availableMem, "Memory available for use"));

        fields.put("configurable", metric.getSample(sample).serverUtil.memory.configurableMem);
        items.add(new MeasurementItem(MeasurementType.GAUGE, MeasurementUnit.MEGABYTES, "configurable",
            metric.getSample(sample).serverUtil.memory.configurableMem, "Memory available and not assigned to partitions"));

        fields.put("assigned", metric.getSample(sample).serverUtil.memory.assignedMemToLpars);
        items.add(new MeasurementItem(MeasurementType.GAUGE, MeasurementUnit.MEGABYTES, "assigned",
            metric.getSample(sample).serverUtil.memory.assignedMemToLpars, "Memory assigned to partitions"));


        fields.put("persistent", metric.getSample(sample).serverUtil.memory.virtualPersistentMem);
        items.add(new MeasurementItem(MeasurementType.GAUGE, MeasurementUnit.MEGABYTES, "persistent",
            metric.getSample(sample).serverUtil.memory.virtualPersistentMem, "Virtual Persistent Memory"));

        log.trace("getMemoryMetrics() - fields: " + fields);
        list.add(new MeasurementBundle(getTimestamp(sample), "system_memory", tags, fields, items));

        return list;
    }


    // System Processor
    List<MeasurementBundle> getProcessorMetrics(int sample) throws NullPointerException {
        log.debug("getProcessorMetrics()");
        List<MeasurementBundle> list = new ArrayList<>();
        HashMap<String, String> tags = new HashMap<>();
        HashMap<String, Object> fields = new HashMap<>();
        List<MeasurementItem> items = new ArrayList<>();

        tags.put("servername", entry.getName());
        log.trace("getProcessorMetrics() - tags: " + tags);

        fields.put("total_units", metric.getSample(sample).serverUtil.processor.totalProcUnits);
        items.add(new MeasurementItem(MeasurementType.GAUGE, "total_units",
            metric.getSample(sample).serverUtil.processor.totalProcUnits, "Processor units installed in system"));

        fields.put("used_units", metric.getSample(sample).serverUtil.processor.utilizedProcUnits);
        items.add(new MeasurementItem(MeasurementType.GAUGE, "used_units",
            metric.getSample(sample).serverUtil.processor.utilizedProcUnits, "Processor units utilized by partitions"));

        fields.put("available_units", metric.getSample(sample).serverUtil.processor.availableProcUnits);
        items.add(new MeasurementItem(MeasurementType.GAUGE, "available_units",
            metric.getSample(sample).serverUtil.processor.availableProcUnits, "Available processor units for use"));

        fields.put("configurable_units", metric.getSample(sample).serverUtil.processor.configurableProcUnits);
        items.add(new MeasurementItem(MeasurementType.GAUGE, "configurable_units",
            metric.getSample(sample).serverUtil.processor.configurableProcUnits, "Processor units available and not used"));

        log.trace("getProcessorMetrics() - fields: " + fields);
        list.add(new MeasurementBundle(getTimestamp(sample), "system_processor", tags, fields, items));
        return list;
    }

    /*
    // Sytem Shared ProcessorPools
    List<MeasurementBundle> getSharedProcessorPools(int sample) throws NullPointerException {
        log.debug("getSharedProcessorPools()");
        List<MeasurementBundle> list = new ArrayList<>();
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

            list.add(new MeasurementBundle(getTimestamp(sample), tagsMap, fieldsMap));
        });

        return list;
    }


    // System Physical ProcessorPool
    List<MeasurementBundle> getPhysicalProcessorPool(int sample) throws NullPointerException {
        log.debug("getPhysicalProcessorPool()");
        List<MeasurementBundle> list = new ArrayList<>();
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

        list.add(new MeasurementBundle(getTimestamp(sample), tagsMap, fieldsMap));

        return list;
    } */


    /**
     * VIO Aggregated Metrics are stored under the Managed System
     */

/*
    // VIO Details
    List<MeasurementBundle> getVioDetails(int sample) throws NullPointerException {
        log.debug("getVioDetails()");
        List<MeasurementBundle> list = new ArrayList<>();
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

            list.add(new MeasurementBundle(getTimestamp(sample), tagsMap, fieldsMap));
        });

        return list;
    }


    // VIO Memory
    List<MeasurementBundle> getVioMemoryMetrics(int sample) throws NullPointerException {
        log.debug("getVioMemoryMetrics()");
        List<MeasurementBundle> list = new ArrayList<>();
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

            list.add(new MeasurementBundle(getTimestamp(sample), tagsMap, fieldsMap));
        });

        return list;
    }


    // VIO Processor
    List<MeasurementBundle> getVioProcessorMetrics(int sample) throws NullPointerException {
        log.debug("getVioProcessorMetrics()");
        List<MeasurementBundle> list = new ArrayList<>();
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

            list.add(new MeasurementBundle(getTimestamp(sample), tagsMap, fieldsMap));
        });

        return list;
    }


    // VIOs - Network
    List<MeasurementBundle> getVioNetworkLpars(int sample) throws NullPointerException {
        log.debug("getVioNetworkLpars()");
        List<MeasurementBundle> list = new ArrayList<>();
        metric.getSample(sample).viosUtil.forEach(vio -> {

            HashMap<String, String> tagsMap = new HashMap<>();
            HashMap<String, Object> fieldsMap = new HashMap<>();

            tagsMap.put("servername", entry.getName());
            tagsMap.put("viosname", vio.name);
            log.trace("getVioNetworkLpars() - tags: " + tagsMap);

            fieldsMap.put("clientlpars", vio.network.clientLpars.size());
            log.trace("getVioNetworkLpars() - fields: " + fieldsMap);

            list.add(new MeasurementBundle(getTimestamp(sample), tagsMap, fieldsMap));
        });

        return list;
    }
*/

    // VIO Network - Shared
    List<MeasurementBundle> getVioNetworkSharedAdapters(int sample) throws NullPointerException {
        log.debug("getVioNetworkSharedAdapters()");
        List<MeasurementBundle> list = new ArrayList<>();
        metric.getSample(sample).viosUtil.forEach(vio -> {

            vio.network.sharedAdapters.forEach(adapter -> {

                HashMap<String, String> tagsMap = new HashMap<>();
                HashMap<String, Object> fieldsMap = new HashMap<>();
                List<MeasurementItem> items = new ArrayList<>();

                tagsMap.put("system", entry.getName());
                tagsMap.put("vios", vio.name);
                tagsMap.put("location", adapter.physicalLocation);
                log.trace("getVioNetworkSharedAdapters() - tags: " + tagsMap);

                fieldsMap.put("id", adapter.id);
                fieldsMap.put("type", adapter.type);

                fieldsMap.put("sentBytes", adapter.sentBytes);
                items.add(new MeasurementItem(MeasurementType.GAUGE, MeasurementUnit.BYTES, "sent",
                    adapter.sentBytes));

                fieldsMap.put("sentPackets", adapter.sentPackets);
                items.add(new MeasurementItem(MeasurementType.GAUGE, MeasurementUnit.PACKETS, "sent",
                    adapter.sentPackets));

                fieldsMap.put("receivedBytes", adapter.receivedBytes);
                items.add(new MeasurementItem(MeasurementType.GAUGE, MeasurementUnit.BYTES, "received",
                    adapter.receivedBytes));

                fieldsMap.put("receivedPackets", adapter.receivedPackets);
                items.add(new MeasurementItem(MeasurementType.GAUGE, MeasurementUnit.PACKETS, "received",
                    adapter.receivedPackets));

                fieldsMap.put("droppedPackets", adapter.droppedPackets);
                items.add(new MeasurementItem(MeasurementType.GAUGE, MeasurementUnit.PACKETS, "dropped",
                    adapter.droppedPackets));

                fieldsMap.put("transferredBytes", adapter.transferredBytes);
                items.add(new MeasurementItem(MeasurementType.GAUGE, MeasurementUnit.BYTES, "transferred",
                    adapter.transferredBytes));

                log.trace("getVioNetworkSharedAdapters() - fields: " + fieldsMap);

                list.add(new MeasurementBundle(getTimestamp(sample), "vio_network_sea", tagsMap, fieldsMap, items));
            });
        });

        return list;
    }
/*

    // VIO Network - Virtual
    List<MeasurementBundle> getVioNetworkVirtualAdapters(int sample) throws NullPointerException {
        log.debug("getVioNetworkVirtualAdapters()");
        List<MeasurementBundle> list = new ArrayList<>();
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

                list.add(new MeasurementBundle(getTimestamp(sample), tagsMap, fieldsMap));
            });
        });

        return list;
    }


    // VIO Network - Generic
    List<MeasurementBundle> getVioNetworkGenericAdapters(int sample) throws NullPointerException {
        log.debug("getVioNetworkGenericAdapters()");
        List<MeasurementBundle> list = new ArrayList<>();
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

                list.add(new MeasurementBundle(getTimestamp(sample), tagsMap, fieldsMap));
            });
        });

        return list;
    }


    // VIOs - Storage
    List<MeasurementBundle> getVioStorageLpars(int sample) throws NullPointerException {
        log.debug("getVioStorageLpars()");
        List<MeasurementBundle> list = new ArrayList<>();
        metric.getSample(sample).viosUtil.forEach(vio -> {

            HashMap<String, String> tagsMap = new HashMap<>();
            HashMap<String, Object> fieldsMap = new HashMap<>();

            tagsMap.put("servername", entry.getName());
            tagsMap.put("viosname", vio.name);
            log.trace("getVioStorageLpars() - tags: " + tagsMap);

            fieldsMap.put("clientlpars", vio.storage.clientLpars.size());
            log.trace("getVioStorageLpars() - fields: " + fieldsMap);

            list.add(new MeasurementBundle(getTimestamp(sample), tagsMap, fieldsMap));
        });

        return list;
    }


    // VIO Storage FC
    List<MeasurementBundle> getVioStorageFiberChannelAdapters(int sample) throws NullPointerException {
        log.debug("getVioStorageFiberChannelAdapters()");
        List<MeasurementBundle> list = new ArrayList<>();
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

                list.add(new MeasurementBundle(getTimestamp(sample), tagsMap, fieldsMap));
            });

        });

        return list;
    }


    // VIO Storage - Physical
    List<MeasurementBundle> getVioStoragePhysicalAdapters(int sample) throws NullPointerException {
        log.debug("getVioStoragePhysicalAdapters()");
        List<MeasurementBundle> list = new ArrayList<>();

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

                list.add(new MeasurementBundle(getTimestamp(sample), tagsMap, fieldsMap));
            });
        });

        return list;
    }


    // VIO Storage - Virtual
    List<MeasurementBundle> getVioStorageVirtualAdapters(int sample) throws NullPointerException {
        log.debug("getVioStorageVirtualAdapters()");
        List<MeasurementBundle> list = new ArrayList<>();
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

                list.add(new MeasurementBundle(getTimestamp(sample), tagsMap, fieldsMap));
            });
        });

        return list;
    }
        */

    /*
    // VIO Storage SSP TODO
    List<Measurement> getViosStorageSharedStoragePools(int sample) throws NullPointerException {

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
