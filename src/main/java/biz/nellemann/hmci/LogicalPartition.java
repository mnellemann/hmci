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


    public String toString() {
        return String.format("[%s] %s (%s)", id, name, type);
    }


    List<Measurement> getAffinityScore() {

        List<Measurement> list = new ArrayList<>();

        Map<String, String> tagsMap = new HashMap<String, String>();
        tagsMap.put("system", system.name);
        tagsMap.put("partition", name);
        log.debug("getAffinityScore() - tags: " + tagsMap.toString());

        Map<String, Number> fieldsMap = new HashMap<String, Number>();
        fieldsMap.put("affinityScore", metrics.systemUtil.sample.lparsUtil.affinityScore);
        log.debug("getAffinityScore() - fields: " + fieldsMap.toString());

        Measurement measurement = new Measurement(tagsMap, fieldsMap);
        list.add(measurement);
        return list;
    }


    List<Measurement> getMemoryMetrics() {

        List<Measurement> list = new ArrayList<>();

        Map<String, String> tagsMap = new HashMap<String, String>();
        tagsMap.put("system", system.name);
        tagsMap.put("partition", name);
        log.debug("getMemoryMetrics() - tags: " + tagsMap.toString());

        Map<String, Number> fieldsMap = new HashMap<String, Number>();
        fieldsMap.put("logicalMem", metrics.systemUtil.sample.lparsUtil.memory.logicalMem);
        fieldsMap.put("backedPhysicalMem", metrics.systemUtil.sample.lparsUtil.memory.backedPhysicalMem);
        log.debug("getMemoryMetrics() - fields: " + fieldsMap.toString());

        Measurement measurement = new Measurement(tagsMap, fieldsMap);
        list.add(measurement);

        return list;
    }

    //@CompileDynamic
    List<Measurement> getProcessorMetrics() {

        List<Measurement> list = new ArrayList<>();

        HashMap<String, String> tagsMap = new HashMap<String, String>();
        tagsMap.put("system", system.name);
        tagsMap.put("partition", name);
        log.debug("getProcessorMetrics() - tags: " + tagsMap.toString());

        HashMap<String, Number> fieldsMap = new HashMap<String, Number>();
        fieldsMap.put("utilizedProcUnits", metrics.systemUtil.sample.lparsUtil.processor.utilizedProcUnits);
        fieldsMap.put("maxVirtualProcessors", metrics.systemUtil.sample.lparsUtil.processor.maxVirtualProcessors);
        fieldsMap.put("currentVirtualProcessors", metrics.systemUtil.sample.lparsUtil.processor.currentVirtualProcessors);
        //fieldsMap.donatedProcUnits: metrics.systemUtil.utilSamples.first().lparsUtil.first().processor.donatedProcUnits.first(),
        fieldsMap.put("entitledProcUnits", metrics.systemUtil.sample.lparsUtil.processor.entitledProcUnits);
        //fieldsMap.idleProcUnits: metrics.systemUtil.utilSamples.first().lparsUtil.first().processor.idleProcUnits.first(),
        //fieldsMap.maxProcUnits: metrics.systemUtil.utilSamples.first().lparsUtil.first().processor.maxProcUnits.first(),
        fieldsMap.put("utilizedCappedProcUnits", metrics.systemUtil.sample.lparsUtil.processor.utilizedCappedProcUnits);
        fieldsMap.put("utilizedUncappedProcUnits", metrics.systemUtil.sample.lparsUtil.processor.utilizedUncappedProcUnits);
        fieldsMap.put("timePerInstructionExecution", metrics.systemUtil.sample.lparsUtil.processor.timeSpentWaitingForDispatch);
        fieldsMap.put("timeSpentWaitingForDispatch", metrics.systemUtil.sample.lparsUtil.processor.timePerInstructionExecution);
        log.debug("getProcessorMetrics() - fields: " + fieldsMap.toString());

        Measurement measurement = new Measurement(tagsMap, fieldsMap);
        list.add(measurement);

        return list;
    }

    //@CompileDynamic
    List<Measurement> getVirtualEthernetAdapterMetrics() {

        List<Measurement> list = new ArrayList<>();
        metrics.systemUtil.sample.lparsUtil.network.virtualEthernetAdapters.forEach( adapter -> {

            HashMap<String, String> tagsMap = new HashMap<String, String>();
            tagsMap.put("system", system.name);
            tagsMap.put("partition", name);
            tagsMap.put("sea", adapter.sharedEthernetAdapterId);
            tagsMap.put("viosId", adapter.viosId.toString());
            tagsMap.put("vlanId", adapter.vlanId.toString());
            tagsMap.put("vswitchId", adapter.vswitchId.toString());
            log.debug("getVirtualEthernetAdapterMetrics() - tags: " + tagsMap.toString());

            HashMap<String, Number> fieldsMap = new HashMap<String, Number>();
            fieldsMap.put("receivedPhysicalBytes", adapter.receivedPhysicalBytes);
            fieldsMap.put("sentPhysicalBytes", adapter.sentPhysicalBytes);
            fieldsMap.put("receivedBytes", adapter.receivedBytes);
            fieldsMap.put("sentBytes", adapter.sentBytes);
            log.debug("getVirtualEthernetAdapterMetrics() - fields: " + fieldsMap.toString());

            Measurement measurement = new Measurement(tagsMap, fieldsMap);
            list.add(measurement);
        });

        return list;
    }


    //PartitionVirtualFiberChannelAdapters
    //@CompileDynamic
    List<Measurement> getVirtualFiberChannelAdaptersMetrics() {

        List<Measurement> list = new ArrayList<>();
        metrics.systemUtil.sample.lparsUtil.storage.virtualFiberChannelAdapters.forEach( adapter -> {

            HashMap<String, String> tagsMap = new HashMap<String, String>();
            tagsMap.put("system", system.name);
            tagsMap.put("partition", name);
            tagsMap.put("viosId", adapter.viosId.toString());
            tagsMap.put("wwpn", adapter.wwpn);
            log.debug("getVirtualFiberChannelAdaptersMetrics() - tags: " + tagsMap.toString());

            HashMap<String, Number> fieldsMap = new HashMap<String, Number>();
            fieldsMap.put("transmittedBytes", adapter.transmittedBytes.get(0));
            fieldsMap.put("writeBytes", adapter.writeBytes.get(0));
            fieldsMap.put("readBytes", adapter.readBytes.get(0));
            log.debug("getVirtualFiberChannelAdaptersMetrics() - fields: " + fieldsMap.toString());

            Measurement measurement = new Measurement(tagsMap, fieldsMap);
            list.add(measurement);
        });

        return list;
    }

}
