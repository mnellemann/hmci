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

import biz.nellemann.hmci.dto.xml.Link;
import biz.nellemann.hmci.dto.xml.LogicalPartitionEntry;
import biz.nellemann.hmci.dto.xml.XmlEntry;
import biz.nellemann.hmci.dto.xml.XmlFeed;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

class LogicalPartition extends Resource {

    private final static Logger log = LoggerFactory.getLogger(LogicalPartition.class);

    private final RestClient restClient;
    private final ManagedSystem managedSystem;

    protected String id;
    protected String name;
    protected LogicalPartitionEntry entry;

    private String uriPath;


    public LogicalPartition(RestClient restClient, String href, ManagedSystem managedSystem) throws URISyntaxException {
        log.debug("LogicalPartition() - {}", href);
        this.restClient = restClient;
        this.managedSystem = managedSystem;
        try {
            URI uri = new URI(href);
            uriPath = uri.getPath();
        } catch (URISyntaxException e) {
            log.error("LogicalPartition() - {}", e.getMessage());
        }
    }


    @Override
    public String toString() {
        return entry.getName();
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
            if(xmlEntry.getContent().isLogicalPartition()) {
                entry = xmlEntry.getContent().getLogicalPartitionEntry();
                this.name = entry.getName();
                log.info("discover() - {}", entry.getName());
            } else {
                throw new UnsupportedOperationException("Failed to deserialize LogicalPartition");
            }

        } catch (Exception e) {
            log.error("discover() - error: {}", e.getMessage());
        }
    }


    public void refresh() {

        log.debug("refresh()");
        try {
            String xml = restClient.getRequest(String.format("/rest/api/pcm/ManagedSystem/%s/LogicalPartition/%s/ProcessedMetrics?NoOfSamples=1", managedSystem.id, id));

            // Do not try to parse empty response
            if(xml == null || xml.length() <= 1) {
                log.warn("refresh() - no data.");
                return;
            }

            XmlMapper xmlMapper = new XmlMapper();
            XmlFeed xmlFeed = xmlMapper.readValue(xml, XmlFeed.class);

            xmlFeed.entries.forEach((entry) -> {
                if(entry.category.term.equals("LogicalPartition")) {
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

        } catch (IOException e) {
            log.error("refresh() - error 2: {}", e.getMessage());
        }

    }



    // LPAR Details
    List<Measurement> getDetails() {

        List<Measurement> list = new ArrayList<>();

        try {
            Map<String, String> tagsMap = new HashMap<>();
            TreeMap<String, Object> fieldsMap = new TreeMap<>();

            tagsMap.put("servername", managedSystem.entry.getName());
            tagsMap.put("lparname", entry.getName());
            log.trace("getDetails() - tags: " + tagsMap);

            fieldsMap.put("id", metric.getSample().lparsUtil.id);
            fieldsMap.put("type", metric.getSample().lparsUtil.type);
            fieldsMap.put("state", metric.getSample().lparsUtil.state);
            fieldsMap.put("osType", metric.getSample().lparsUtil.osType);
            fieldsMap.put("affinityScore", metric.getSample().lparsUtil.affinityScore);
            log.trace("getDetails() - fields: " + fieldsMap);

            list.add(new Measurement(tagsMap, fieldsMap));
        } catch (Exception e) {
            log.warn("getDetails() - error: {}", e.getMessage());
        }

        return list;
    }


    // LPAR Memory
    List<Measurement> getMemoryMetrics() {

        List<Measurement> list = new ArrayList<>();

        try {
            Map<String, String> tagsMap = new HashMap<>();
            TreeMap<String, Object> fieldsMap = new TreeMap<>();

            tagsMap.put("servername", managedSystem.entry.getName());
            tagsMap.put("lparname", entry.getName());
            log.trace("getMemoryMetrics() - tags: " + tagsMap);

            fieldsMap.put("logicalMem", metric.getSample().lparsUtil.memory.logicalMem);
            fieldsMap.put("backedPhysicalMem", metric.getSample().lparsUtil.memory.backedPhysicalMem);
            log.trace("getMemoryMetrics() - fields: " + fieldsMap);

            list.add(new Measurement(tagsMap, fieldsMap));
        } catch (Exception e) {
            log.warn("getMemoryMetrics() - error: {}", e.getMessage());
        }
        return list;
    }


    // LPAR Processor
    List<Measurement> getProcessorMetrics() {

        List<Measurement> list = new ArrayList<>();

        try {
            HashMap<String, String> tagsMap = new HashMap<>();
            HashMap<String, Object> fieldsMap = new HashMap<>();

            tagsMap.put("servername", managedSystem.entry.getName());
            tagsMap.put("lparname", entry.getName());
            log.trace("getProcessorMetrics() - tags: " + tagsMap);

            fieldsMap.put("utilizedProcUnits", metric.getSample().lparsUtil.processor.utilizedProcUnits);
            fieldsMap.put("entitledProcUnits", metric.getSample().lparsUtil.processor.entitledProcUnits);
            fieldsMap.put("donatedProcUnits", metric.getSample().lparsUtil.processor.donatedProcUnits);
            fieldsMap.put("idleProcUnits", metric.getSample().lparsUtil.processor.idleProcUnits);
            fieldsMap.put("maxProcUnits", metric.getSample().lparsUtil.processor.maxProcUnits);
            fieldsMap.put("maxVirtualProcessors", metric.getSample().lparsUtil.processor.maxVirtualProcessors);
            fieldsMap.put("currentVirtualProcessors", metric.getSample().lparsUtil.processor.currentVirtualProcessors);
            fieldsMap.put("utilizedCappedProcUnits", metric.getSample().lparsUtil.processor.utilizedCappedProcUnits);
            fieldsMap.put("utilizedUncappedProcUnits", metric.getSample().lparsUtil.processor.utilizedUncappedProcUnits);
            fieldsMap.put("timePerInstructionExecution", metric.getSample().lparsUtil.processor.timeSpentWaitingForDispatch);
            fieldsMap.put("timeSpentWaitingForDispatch", metric.getSample().lparsUtil.processor.timePerInstructionExecution);
            fieldsMap.put("mode", metric.getSample().lparsUtil.processor.mode);
            fieldsMap.put("weight", metric.getSample().lparsUtil.processor.weight);
            fieldsMap.put("poolId", metric.getSample().lparsUtil.processor.poolId);
            log.trace("getProcessorMetrics() - fields: " + fieldsMap);

            list.add(new Measurement(tagsMap, fieldsMap));
        } catch (Exception e) {
            log.warn("getProcessorMetrics() - error: {}", e.getMessage());
        }

        return list;
    }


    // LPAR Network - Virtual
    List<Measurement> getVirtualEthernetAdapterMetrics() {

        List<Measurement> list = new ArrayList<>();

        try {
            metric.getSample().lparsUtil.network.virtualEthernetAdapters.forEach(adapter -> {

                HashMap<String, String> tagsMap = new HashMap<>();
                HashMap<String, Object> fieldsMap = new HashMap<>();

                tagsMap.put("servername", managedSystem.entry.getName());
                tagsMap.put("lparname", entry.getName());
                tagsMap.put("location", adapter.physicalLocation);
                tagsMap.put("viosId", adapter.viosId.toString());
                tagsMap.put("vlanId", adapter.vlanId.toString());
                tagsMap.put("vswitchId", adapter.vswitchId.toString());
                log.trace("getVirtualEthernetAdapterMetrics() - tags: " + tagsMap);

                fieldsMap.put("droppedPackets", adapter.droppedPackets);
                fieldsMap.put("droppedPhysicalPackets", adapter.droppedPhysicalPackets);
                fieldsMap.put("isPortVlanId", adapter.isPortVlanId);
                fieldsMap.put("receivedPhysicalBytes", adapter.receivedPhysicalBytes);
                fieldsMap.put("receivedPhysicalPackets", adapter.receivedPhysicalPackets);
                fieldsMap.put("sentPhysicalBytes", adapter.sentPhysicalBytes);
                fieldsMap.put("sentPhysicalPackets", adapter.sentPhysicalPackets);
                fieldsMap.put("receivedBytes", adapter.receivedBytes);
                fieldsMap.put("receivedPackets", adapter.receivedPackets);
                fieldsMap.put("sentBytes", adapter.sentBytes);
                fieldsMap.put("sentPackets", adapter.sentPackets);
                fieldsMap.put("transferredBytes", adapter.transferredBytes);
                fieldsMap.put("transferredPhysicalBytes", adapter.transferredPhysicalBytes);
                fieldsMap.put("sharedEthernetAdapterId", adapter.sharedEthernetAdapterId);
                log.trace("getVirtualEthernetAdapterMetrics() - fields: " + fieldsMap);

                list.add(new Measurement(tagsMap, fieldsMap));
            });
        } catch (Exception e) {
            log.warn("getVirtualEthernetAdapterMetrics() - error: {}", e.getMessage());
        }

        return list;
    }


    // LPAR Storage - Virtual Generic
    List<Measurement> getVirtualGenericAdapterMetrics() {

        List<Measurement> list = new ArrayList<>();

        try {
            metric.getSample().lparsUtil.storage.genericVirtualAdapters.forEach(adapter -> {

                HashMap<String, String> tagsMap = new HashMap<>();
                HashMap<String, Object> fieldsMap = new HashMap<>();

                tagsMap.put("servername", managedSystem.entry.getName());
                tagsMap.put("lparname", entry.getName());
                tagsMap.put("viosId", adapter.viosId.toString());
                tagsMap.put("location", adapter.physicalLocation);
                tagsMap.put("id", adapter.id);
                log.trace("getVirtualGenericAdapterMetrics() - tags: " + tagsMap);

                fieldsMap.put("numOfReads", adapter.numOfReads);
                fieldsMap.put("numOfWrites", adapter.numOfWrites);
                fieldsMap.put("writeBytes", adapter.writeBytes);
                fieldsMap.put("readBytes", adapter.readBytes);
                fieldsMap.put("type", adapter.type);
                log.trace("getVirtualGenericAdapterMetrics() - fields: " + fieldsMap);

                list.add(new Measurement(tagsMap, fieldsMap));
            });
        } catch (Exception e) {
            log.warn("getVirtualGenericAdapterMetrics() - error: {}", e.getMessage());
        }


        return list;
    }

    // LPAR Storage - Virtual FC
    List<Measurement> getVirtualFibreChannelAdapterMetrics() {

        List<Measurement> list = new ArrayList<>();

        try {
            metric.getSample().lparsUtil.storage.virtualFiberChannelAdapters.forEach(adapter -> {

                HashMap<String, String> tagsMap = new HashMap<>();
                HashMap<String, Object> fieldsMap = new HashMap<>();

                tagsMap.put("servername", managedSystem.entry.getName());
                tagsMap.put("lparname", entry.getName());
                tagsMap.put("viosId", adapter.viosId.toString());
                tagsMap.put("location", adapter.physicalLocation);
                log.trace("getVirtualFibreChannelAdapterMetrics() - tags: " + tagsMap);

                fieldsMap.put("numOfReads", adapter.numOfReads);
                fieldsMap.put("numOfWrites", adapter.numOfWrites);
                fieldsMap.put("writeBytes", adapter.writeBytes);
                fieldsMap.put("readBytes", adapter.readBytes);
                fieldsMap.put("runningSpeed", adapter.runningSpeed);
                fieldsMap.put("transmittedBytes", adapter.transmittedBytes);
                fieldsMap.put("transferredByte", adapter.transmittedBytes); // TODO: Must be error in dashboard, remove when checked.
                log.trace("getVirtualFibreChannelAdapterMetrics() - fields: " + fieldsMap);

                list.add(new Measurement(tagsMap, fieldsMap));
            });
        } catch (Exception e) {
            log.warn("getVirtualFibreChannelAdapterMetrics() - error: {}", e.getMessage());
        }

        return list;
    }


    // LPAR Network - SR-IOV Logical Ports
    List<Measurement> getSriovLogicalPorts() {

        List<Measurement> list = new ArrayList<>();

        try {
            metric.getSample().lparsUtil.network.sriovLogicalPorts.forEach(port -> {

                HashMap<String, String> tagsMap = new HashMap<>();
                HashMap<String, Object> fieldsMap = new HashMap<>();

                tagsMap.put("servername", managedSystem.entry.getName());
                tagsMap.put("lparname", entry.getName());
                tagsMap.put("location", port.physicalLocation);
                tagsMap.put("type", port.configurationType);
                log.trace("getSriovLogicalPorts() - tags: " + tagsMap);

                fieldsMap.put("sentBytes", port.sentBytes);
                fieldsMap.put("receivedBytes", port.receivedBytes);
                fieldsMap.put("transferredBytes", port.transferredBytes);
                fieldsMap.put("sentPackets", port.sentPackets);
                fieldsMap.put("receivedPackets", port.receivedPackets);
                fieldsMap.put("droppedPackets", port.droppedPackets);
                fieldsMap.put("errorIn", port.errorIn);
                fieldsMap.put("errorOut", port.errorOut);
                log.trace("getSriovLogicalPorts() - fields: " + fieldsMap);

                list.add(new Measurement(tagsMap, fieldsMap));
            });
        } catch (Exception e) {
            log.warn("getSriovLogicalPorts() - error: {}", e.getMessage());
        }

        return list;
    }
}
