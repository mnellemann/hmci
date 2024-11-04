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
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

class LogicalPartition extends Resource {

    private final static Logger log = LoggerFactory.getLogger(LogicalPartition.class);

    private final Session session;
    private final ManagedSystem managedSystem;

    protected String id;
    protected String name;
    protected LogicalPartitionEntry entry;

    private String uriPath;


    public LogicalPartition(Session session, ManagedSystem managedSystem, String href) {
        log.debug("LogicalPartition() - {}", href);
        this.session = session;
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
            String xml = session.getRestClient().getRequest(uriPath);

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
                log.info("discover() - [{}] {} ({})", entry.partitionId, entry.getName(), entry.operatingSystemType);
            } else {
                throw new UnsupportedOperationException("Failed to deserialize LogicalPartition");
            }

        } catch (Exception e) {
            log.error("discover() - error: {}", e.getMessage());
        }
    }


    public void refresh() {

        log.debug("refresh() - {}", name);
        try {
            String xml = session.getRestClient().getRequest(String.format("/rest/api/pcm/ManagedSystem/%s/LogicalPartition/%s/ProcessedMetrics?NoOfSamples=%d", managedSystem.id, id, noOfSamples));

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
                            String json = session.getRestClient().getRequest(jsonUri.getPath());
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


    @Override
    public void process(int sample) throws NullPointerException {
        log.debug("process() - {} - sample: {}", name, sample);

        session.writeMetric(getInformation(sample));
        session.writeMetric(getMemoryMetrics(sample));
        session.writeMetric(getProcessorMetrics(sample));
        session.writeMetric(getSriovLogicalPorts(sample));
        session.writeMetric(getVirtualEthernetAdapterMetrics(sample));
        session.writeMetric(getGenericVirtualAdapterMetrics(sample));
        session.writeMetric(getGenericPhysicalAdapterMetrics(sample));
        session.writeMetric(getFibreChannelAdapterMetrics(sample));
        session.writeMetric(getVirtualFibreChannelAdapterMetrics(sample));
    }


    // LPAR Details
    List<MeasurementBundle> getInformation(int sample) throws NullPointerException {

        log.debug("getInformation()");
        List<MeasurementBundle> bundles = new ArrayList<>();

        Map<String, String> tags = new HashMap<>();
        List<MeasurementItem> items = new ArrayList<>();

        tags.put("system", managedSystem.entry.getName());
        tags.put("partition", entry.getName());
        log.trace("getInformation() - tags: " + tags);

        items.add(new MeasurementItem(MeasurementType.INFO, "id", metric.getSample(sample).lparsUtil.id));
        items.add(new MeasurementItem(MeasurementType.INFO, "type", metric.getSample(sample).lparsUtil.type));
        items.add(new MeasurementItem(MeasurementType.INFO, "state", metric.getSample(sample).lparsUtil.state));
        items.add(new MeasurementItem(MeasurementType.INFO, "os_type", metric.getSample(sample).lparsUtil.osType));
        items.add(new MeasurementItem(MeasurementType.GAUGE, MeasurementUnit.RATIO, "affinity", metric.getSample(sample).lparsUtil.affinityScore));

        log.trace("getInformation() - items: " + items);
        bundles.add(new MeasurementBundle(getTimestamp(sample), "partition_info", tags, items));

        return bundles;
    }


    // LPAR Memory
    List<MeasurementBundle> getMemoryMetrics(int sample) throws NullPointerException {
        log.debug("getMemoryMetrics()");
        List<MeasurementBundle> bundles = new ArrayList<>();

        Map<String, String> tags = new HashMap<>();
        List<MeasurementItem> items = new ArrayList<>();

        tags.put("system", managedSystem.entry.getName());
        tags.put("partition", entry.getName());
        log.trace("getMemoryMetrics() - tags: " + tags);

        items.add(new MeasurementItem(MeasurementType.GAUGE, MeasurementUnit.MB, "logical",
            metric.getSample(sample).lparsUtil.memory.logicalMem));
        log.trace("getMemoryMetrics() - items: " + items);

        bundles.add(new MeasurementBundle(getTimestamp(sample), "partition_memory", tags, items));
        return bundles;
    }


    // LPAR Processor
    List<MeasurementBundle> getProcessorMetrics(int sample) throws NullPointerException {
        log.debug("getProcessorMetrics()");
        List<MeasurementBundle> list = new ArrayList<>();

        HashMap<String, String> tags = new HashMap<>();
        List<MeasurementItem> items = new ArrayList<>();

        tags.put("system", managedSystem.entry.getName());
        tags.put("partition", entry.getName());
        log.trace("getProcessorMetrics() - tags: " + tags);

        items.add(new MeasurementItem(MeasurementType.GAUGE, MeasurementUnit.UNITS, "utilized",
            metric.getSample(sample).lparsUtil.processor.utilizedProcUnits));
        items.add(new MeasurementItem(MeasurementType.GAUGE, MeasurementUnit.UNITS, "entitled",
            metric.getSample(sample).lparsUtil.processor.entitledProcUnits));
        items.add(new MeasurementItem(MeasurementType.GAUGE, MeasurementUnit.UNITS, "donated",
            metric.getSample(sample).lparsUtil.processor.donatedProcUnits));
        items.add(new MeasurementItem(MeasurementType.GAUGE, MeasurementUnit.UNITS, "idle",
            metric.getSample(sample).lparsUtil.processor.idleProcUnits));
        items.add(new MeasurementItem(MeasurementType.GAUGE, MeasurementUnit.UNITS, "vp",
            metric.getSample(sample).lparsUtil.processor.currentVirtualProcessors, "Power cores visible to operating system."));
        items.add(new MeasurementItem(MeasurementType.INFO, "weight", metric.getSample(sample).lparsUtil.processor.weight));
        items.add(new MeasurementItem(MeasurementType.INFO, "mode", metric.getSample(sample).lparsUtil.processor.mode));


        //fieldsMap.put("maxProcUnits", metric.getSample(sample).lparsUtil.processor.maxProcUnits);
        //fieldsMap.put("maxVirtualProcessors", metric.getSample(sample).lparsUtil.processor.maxVirtualProcessors);
        //fieldsMap.put("utilizedCappedProcUnits", metric.getSample(sample).lparsUtil.processor.utilizedCappedProcUnits);
        //fieldsMap.put("utilizedUncappedProcUnits", metric.getSample(sample).lparsUtil.processor.utilizedUncappedProcUnits);
        //fieldsMap.put("timePerInstructionExecution", metric.getSample(sample).lparsUtil.processor.timeSpentWaitingForDispatch);
        //fieldsMap.put("timeSpentWaitingForDispatch", metric.getSample(sample).lparsUtil.processor.timePerInstructionExecution);
        //fieldsMap.put("poolId", metric.getSample(sample).lparsUtil.processor.poolId);

        log.trace("getProcessorMetrics() - items: " + items);
        list.add(new MeasurementBundle(getTimestamp(sample), "partition_processor", tags, items));

        return list;
    }


    // LPAR Network - Virtual
    List<MeasurementBundle> getVirtualEthernetAdapterMetrics(int sample) throws NullPointerException {
        log.debug("getVirtualEthernetAdapterMetrics()");
        List<MeasurementBundle> list = new ArrayList<>();

        metric.getSample(sample).lparsUtil.network.virtualEthernetAdapters.forEach(adapter -> {

            HashMap<String, String> tags = new HashMap<>();
            List<MeasurementItem> items = new ArrayList<>();

            tags.put("system", managedSystem.entry.getName());
            tags.put("partition", entry.getName());
            tags.put("location", adapter.physicalLocation);
            tags.put("vlan", String.valueOf(adapter.vlanId));
            log.trace("getVirtualEthernetAdapterMetrics() - tags: " + tags);

            items.add(new MeasurementItem(MeasurementType.GAUGE, MeasurementUnit.PACKETS, "dropped",
                adapter.droppedPackets));
            items.add(new MeasurementItem(MeasurementType.GAUGE, MeasurementUnit.BYTES, "received",
                adapter.receivedBytes));
            items.add(new MeasurementItem(MeasurementType.GAUGE, MeasurementUnit.PACKETS, "received",
                adapter.receivedPackets));
            items.add(new MeasurementItem(MeasurementType.GAUGE, MeasurementUnit.BYTES, "sent",
                adapter.sentBytes));
            items.add(new MeasurementItem(MeasurementType.GAUGE, MeasurementUnit.PACKETS, "sent",
                adapter.sentPackets));
            log.trace("getVirtualEthernetAdapterMetrics() - items: " + items);

            list.add(new MeasurementBundle(getTimestamp(sample), "partition_network_virtual", tags, items));
        });

        return list;
    }


    // LPAR Storage - Generic Virtual
    List<MeasurementBundle> getGenericVirtualAdapterMetrics(int sample) throws NullPointerException {
        log.debug("getGenericVirtualAdapterMetrics()");
        List<MeasurementBundle> list = new ArrayList<>();

        metric.getSample(sample).lparsUtil.storage.genericVirtualAdapters.forEach(adapter -> {

            HashMap<String, String> tags = new HashMap<>();
            List<MeasurementItem> items = new ArrayList<>();

            tags.put("system", managedSystem.entry.getName());
            tags.put("partition", entry.getName());
            tags.put("location", adapter.physicalLocation);
            log.trace("getGenericVirtualAdapterMetrics() - tags: " + tags);

            items.add(new MeasurementItem(MeasurementType.INFO, "type", adapter.type));
            items.add(new MeasurementItem(MeasurementType.GAUGE, MeasurementUnit.BYTES, "write",
                adapter.writeBytes));
            items.add(new MeasurementItem(MeasurementType.GAUGE, MeasurementUnit.BYTES, "read",
                adapter.readBytes));
            log.trace("getGenericVirtualAdapterMetrics() - items: " + items);

            list.add(new MeasurementBundle(getTimestamp(sample), "partition_storage_generic_virtual", tags, items));
        });

        return list;
    }

    // LPAR Storage - Generic Physical
    List<MeasurementBundle> getGenericPhysicalAdapterMetrics(int sample) throws NullPointerException {
        log.debug("getGenericPhysicalAdapterMetrics()");
        List<MeasurementBundle> list = new ArrayList<>();

        metric.getSample(sample).lparsUtil.storage.genericPhysicalAdapters.forEach(adapter -> {

            HashMap<String, String> tags = new HashMap<>();
            List<MeasurementItem> items = new ArrayList<>();

            tags.put("system", managedSystem.entry.getName());
            tags.put("partition", entry.getName());
            tags.put("location", adapter.physicalLocation);
            log.trace("getGenericPhysicalAdapterMetrics() - tags: " + tags);

            items.add(new MeasurementItem(MeasurementType.INFO, "type", adapter.type));
            items.add(new MeasurementItem(MeasurementType.GAUGE, MeasurementUnit.BYTES, "write",
                adapter.writeBytes));
            items.add(new MeasurementItem(MeasurementType.GAUGE, MeasurementUnit.BYTES, "read",
                adapter.readBytes));
            log.trace("getGenericPhysicalAdapterMetrics() - items: " + items);

            list.add(new MeasurementBundle(getTimestamp(sample), "partition_storage_generic_physical", tags, items));
        });

        return list;
    }

    // LPAR Storage - Virtual FC
    List<MeasurementBundle> getVirtualFibreChannelAdapterMetrics(int sample) throws NullPointerException {
        log.debug("getVirtualFibreChannelAdapterMetrics()");
        List<MeasurementBundle> list = new ArrayList<>();

        metric.getSample(sample).lparsUtil.storage.virtualFiberChannelAdapters.forEach(adapter -> {

            HashMap<String, String> tags = new HashMap<>();
            List<MeasurementItem> items = new ArrayList<>();

            tags.put("system", managedSystem.entry.getName());
            tags.put("partition", entry.getName());
            //tagsMap.put("vios", String.valueOf(adapter.viosId));
            tags.put("location", adapter.physicalLocation);
            log.trace("getVirtualFibreChannelAdapterMetrics() - tags: " + tags);

            //fieldsMap.put("numOfReads", adapter.numOfReads);
            //fieldsMap.put("numOfWrites", adapter.numOfWrites);
            //fieldsMap.put("runningSpeed", adapter.runningSpeed);

            items.add(new MeasurementItem(MeasurementType.GAUGE, MeasurementUnit.BYTES, "write",
                adapter.writeBytes));

            items.add(new MeasurementItem(MeasurementType.GAUGE, MeasurementUnit.BYTES, "read",
                adapter.readBytes));

            /*
            fieldsMap.put("transmitted_bytes", adapter.transmittedBytes);
            items.add(new MeasurementItem(MeasurementType.GAUGE, MeasurementUnit.BYTES, "transmitted",
                adapter.transmittedBytes));
             */

            log.trace("getVirtualFibreChannelAdapterMetrics() - items: " + items);
            list.add(new MeasurementBundle(getTimestamp(sample), "partition_storage_vfc", tags, items));
        });

        return list;
    }


    // LPAR Storage - Fibre Channel
    List<MeasurementBundle> getFibreChannelAdapterMetrics(int sample) throws NullPointerException {
        log.debug("getFibreChannelAdapterMetrics()");
        List<MeasurementBundle> list = new ArrayList<>();

        metric.getSample(sample).lparsUtil.storage.fiberChannelAdapters.forEach(adapter -> {

            HashMap<String, String> tags = new HashMap<>();
            List<MeasurementItem> items = new ArrayList<>();

            tags.put("system", managedSystem.entry.getName());
            tags.put("partition", entry.getName());
            tags.put("location", adapter.physicalLocation);
            log.trace("getFibreChannelAdapterMetrics() - tags: " + tags);

            items.add(new MeasurementItem(MeasurementType.GAUGE, MeasurementUnit.BYTES, "write",
                adapter.writeBytes));
            items.add(new MeasurementItem(MeasurementType.GAUGE, MeasurementUnit.BYTES, "read",
                adapter.readBytes));
            log.trace("getFibreChannelAdapterMetrics() - items: " + items);

            list.add(new MeasurementBundle(getTimestamp(sample), "partition_storage_fc", tags, items));
        });

        return list;
    }

    // LPAR Network - SR-IOV Logical Ports
    List<MeasurementBundle> getSriovLogicalPorts(int sample) throws NullPointerException {
        log.debug("getSriovLogicalPorts()");
        List<MeasurementBundle> list = new ArrayList<>();

        metric.getSample(sample).lparsUtil.network.sriovLogicalPorts.forEach(port -> {

            HashMap<String, String> tags = new HashMap<>();
            List<MeasurementItem> items = new ArrayList<>();

            tags.put("system", managedSystem.entry.getName());
            tags.put("partition", entry.getName());
            tags.put("location", port.physicalLocation);
            log.trace("getSriovLogicalPorts() - tags: " + tags);

            //fieldsMap.put("errorIn", port.errorIn);
            //fieldsMap.put("errorOut", port.errorOut);

            items.add(new MeasurementItem(MeasurementType.GAUGE, MeasurementUnit.BYTES, "sent",
                port.sentBytes));

            items.add(new MeasurementItem(MeasurementType.GAUGE, MeasurementUnit.BYTES, "received",
                port.receivedBytes));

            items.add(new MeasurementItem(MeasurementType.GAUGE, MeasurementUnit.PACKETS, "sent",
                port.sentPackets));

            items.add(new MeasurementItem(MeasurementType.GAUGE, MeasurementUnit.PACKETS, "received",
                port.receivedPackets        ));

            items.add(new MeasurementItem(MeasurementType.GAUGE, MeasurementUnit.PACKETS, "dropped",
                port.droppedPackets));

            log.trace("getSriovLogicalPorts() - items: " + items);
            list.add(new MeasurementBundle(getTimestamp(sample), "partition_network_sriov", tags, items));
        });

        return list;
    }

}
