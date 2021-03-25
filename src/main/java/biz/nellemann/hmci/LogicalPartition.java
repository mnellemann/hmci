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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class LogicalPartition extends MetaSystem {

    private final static Logger log = LoggerFactory.getLogger(LogicalPartition.class);

    public final String id;
    public final String name;
    public final String type;
    public final ManagedSystem system;


    LogicalPartition(String id, String name, String type, ManagedSystem system) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.system = system;
    }


    @Override
    public String toString() {
        return String.format("[%s] %s (%s)", id, name, type);
    }


    // LPAR Details
    List<Measurement> getDetails() {

        List<Measurement> list = new ArrayList<>();

        Map<String, String> tagsMap = new HashMap<>();
        tagsMap.put("servername", system.name);
        tagsMap.put("lparname", name);
        log.trace("getDetails() - tags: " + tagsMap.toString());

        Map<String, Object> fieldsMap = new HashMap<>();
        fieldsMap.put("id", metrics.systemUtil.sample.lparsUtil.id);
        fieldsMap.put("type", metrics.systemUtil.sample.lparsUtil.type);
        fieldsMap.put("state", metrics.systemUtil.sample.lparsUtil.state);
        fieldsMap.put("osType", metrics.systemUtil.sample.lparsUtil.osType);
        fieldsMap.put("affinityScore", metrics.systemUtil.sample.lparsUtil.affinityScore);
        log.trace("getDetails() - fields: " + fieldsMap.toString());

        list.add(new Measurement(tagsMap, fieldsMap));
        return list;
    }


    // LPAR Memory
    List<Measurement> getMemoryMetrics() {

        List<Measurement> list = new ArrayList<>();

        Map<String, String> tagsMap = new HashMap<>();
        tagsMap.put("servername", system.name);
        tagsMap.put("lparname", name);
        log.trace("getMemoryMetrics() - tags: " + tagsMap.toString());

        Map<String, Object> fieldsMap = new HashMap<>();
        fieldsMap.put("logicalMem", metrics.systemUtil.sample.lparsUtil.memory.logicalMem);
        fieldsMap.put("backedPhysicalMem", metrics.systemUtil.sample.lparsUtil.memory.backedPhysicalMem);
        log.trace("getMemoryMetrics() - fields: " + fieldsMap.toString());

        list.add(new Measurement(tagsMap, fieldsMap));
        return list;
    }


    // LPAR Processor
    List<Measurement> getProcessorMetrics() {

        List<Measurement> list = new ArrayList<>();

        HashMap<String, String> tagsMap = new HashMap<>();
        tagsMap.put("servername", system.name);
        tagsMap.put("lparname", name);
        log.trace("getProcessorMetrics() - tags: " + tagsMap.toString());

        HashMap<String, Object> fieldsMap = new HashMap<>();
        fieldsMap.put("utilizedProcUnits", metrics.systemUtil.sample.lparsUtil.processor.utilizedProcUnits);
        fieldsMap.put("entitledProcUnits", metrics.systemUtil.sample.lparsUtil.processor.entitledProcUnits);
        fieldsMap.put("donatedProcUnits", metrics.systemUtil.sample.lparsUtil.processor.donatedProcUnits);
        fieldsMap.put("idleProcUnits", metrics.systemUtil.sample.lparsUtil.processor.idleProcUnits);
        fieldsMap.put("maxProcUnits", metrics.systemUtil.sample.lparsUtil.processor.maxProcUnits);
        fieldsMap.put("maxVirtualProcessors", metrics.systemUtil.sample.lparsUtil.processor.maxVirtualProcessors);
        fieldsMap.put("currentVirtualProcessors", metrics.systemUtil.sample.lparsUtil.processor.currentVirtualProcessors);
        fieldsMap.put("utilizedCappedProcUnits", metrics.systemUtil.sample.lparsUtil.processor.utilizedCappedProcUnits);
        fieldsMap.put("utilizedUncappedProcUnits", metrics.systemUtil.sample.lparsUtil.processor.utilizedUncappedProcUnits);
        fieldsMap.put("timePerInstructionExecution", metrics.systemUtil.sample.lparsUtil.processor.timeSpentWaitingForDispatch);
        fieldsMap.put("timeSpentWaitingForDispatch", metrics.systemUtil.sample.lparsUtil.processor.timePerInstructionExecution);
        fieldsMap.put("mode", metrics.systemUtil.sample.lparsUtil.processor.mode);
        fieldsMap.put("weight", metrics.systemUtil.sample.lparsUtil.processor.weight);
        fieldsMap.put("poolId", metrics.systemUtil.sample.lparsUtil.processor.poolId);
        log.trace("getProcessorMetrics() - fields: " + fieldsMap.toString());

        list.add(new Measurement(tagsMap, fieldsMap));
        return list;
    }


    // LPAR Network - Virtual
    List<Measurement> getVirtualEthernetAdapterMetrics() {

        List<Measurement> list = new ArrayList<>();

        metrics.systemUtil.sample.lparsUtil.network.virtualEthernetAdapters.forEach( adapter -> {

            HashMap<String, String> tagsMap = new HashMap<>();
            tagsMap.put("servername", system.name);
            tagsMap.put("lparname", name);
            tagsMap.put("location", adapter.physicalLocation);
            tagsMap.put("viosId", adapter.viosId.toString());
            tagsMap.put("vlanId", adapter.vlanId.toString());
            tagsMap.put("vswitchId", adapter.vswitchId.toString());
            log.trace("getVirtualEthernetAdapterMetrics() - tags: " + tagsMap.toString());

            HashMap<String, Object> fieldsMap = new HashMap<>();
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
            log.trace("getVirtualEthernetAdapterMetrics() - fields: " + fieldsMap.toString());

            list.add(new Measurement(tagsMap, fieldsMap));
        });

        return list;
    }


    // LPAR Storage - Virtual Generic
    List<Measurement> getVirtualGenericAdapterMetrics() {

        List<Measurement> list = new ArrayList<>();
        metrics.systemUtil.sample.lparsUtil.storage.genericVirtualAdapters.forEach( adapter -> {

            HashMap<String, String> tagsMap = new HashMap<>();
            tagsMap.put("servername", system.name);
            tagsMap.put("lparname", name);
            tagsMap.put("viosId", adapter.viosId.toString());
            tagsMap.put("location", adapter.physicalLocation);
            tagsMap.put("id", adapter.id);
            log.trace("getVirtualGenericAdapterMetrics() - tags: " + tagsMap.toString());

            HashMap<String, Object> fieldsMap = new HashMap<>();
            fieldsMap.put("numOfReads", adapter.numOfReads);
            fieldsMap.put("numOfWrites", adapter.numOfWrites);
            fieldsMap.put("writeBytes", adapter.writeBytes);
            fieldsMap.put("readBytes", adapter.readBytes);
            fieldsMap.put("type", adapter.type);
            log.trace("getVirtualGenericAdapterMetrics() - fields: " + fieldsMap.toString());

            list.add(new Measurement(tagsMap, fieldsMap));
        });

        return list;
    }

    // LPAR Storage - Virtual FC
    List<Measurement> getVirtualFibreChannelAdapterMetrics() {

        List<Measurement> list = new ArrayList<>();
        metrics.systemUtil.sample.lparsUtil.storage.virtualFiberChannelAdapters.forEach( adapter -> {

            HashMap<String, String> tagsMap = new HashMap<>();
            tagsMap.put("servername", system.name);
            tagsMap.put("lparname", name);
            tagsMap.put("viosId", adapter.viosId.toString());
            tagsMap.put("location", adapter.physicalLocation);
            log.trace("getVirtualFibreChannelAdapterMetrics() - tags: " + tagsMap.toString());

            HashMap<String, Object> fieldsMap = new HashMap<>();
            fieldsMap.put("numOfReads", adapter.numOfReads);
            fieldsMap.put("numOfWrites", adapter.numOfWrites);
            fieldsMap.put("writeBytes", adapter.writeBytes);
            fieldsMap.put("readBytes", adapter.readBytes);
            //fieldsMap.put("transmittedBytes", adapter.transmittedBytes);
            //fieldsMap.put("wwpn", adapter.wwpn);
            //fieldsMap.put("wwpn2", adapter.wwpn2);
            log.trace("getVirtualFibreChannelAdapterMetrics() - fields: " + fieldsMap.toString());

            list.add(new Measurement(tagsMap, fieldsMap));
        });

        return list;
    }


}
