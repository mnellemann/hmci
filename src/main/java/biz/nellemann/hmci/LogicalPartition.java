/**
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

    public String id;
    public String name;
    public String type;
    public ManagedSystem system;


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
        //Map<String, Map> map = new HashMap<String, Map>()

        Map<String, String> tagsMap = new HashMap<String, String>() {
            {
                put("system", system.name);
                put("partition", name);
            }
        };

        //map.put("tags", tagsMap)
        log.debug("getAffinityScore() - tags: " + tagsMap.toString());
        Map<String, Number> fieldsMap = new HashMap<String, Number>() {
            {
                put("affinityScore", metrics.systemUtil.sample.lparsUtil.affinityScore);
            }
        };

        //map.put("fields", fieldsMap)
        log.debug("getAffinityScore() - fields: " + fieldsMap.toString());

        Measurement measurement = new Measurement(tagsMap, fieldsMap);
        list.add(measurement);
        return list;
    }


    List<Measurement> getMemoryMetrics() {

        List<Measurement> list = new ArrayList<>();
        //Map<String, Map> map = new HashMap<String, Map>()

        Map<String, String> tagsMap = new HashMap<String, String>() {
            {
                put("system", system.name);
                put("partition", name);
            }
        };

        //map.put("tags", tagsMap)
        log.debug("getMemoryMetrics() - tags: " + tagsMap.toString());

        Map<String, Number> fieldsMap = new HashMap<String, Number>() {
            {
                put("logicalMem", metrics.systemUtil.sample.lparsUtil.memory.logicalMem);
                put("backedPhysicalMem", metrics.systemUtil.sample.lparsUtil.memory.backedPhysicalMem);
            }
        };


        //map.put("fields", fieldsMap)
        log.debug("getMemoryMetrics() - fields: " + fieldsMap.toString());

        Measurement measurement = new Measurement(tagsMap, fieldsMap);
        list.add(measurement);

        return list;
    }

    //@CompileDynamic
    List<Measurement> getProcessorMetrics() {

        List<Measurement> list = new ArrayList<>();
        //Map<String, Map> map = new HashMap<String, Map>()

        HashMap<String, String> tagsMap = new HashMap<String, String>() {
            {
                put("system", system.name);
                put("partition", name);
            }
        };

        //map.put("tags", tagsMap)
        log.debug("getProcessorMetrics() - tags: " + tagsMap.toString());

        HashMap<String, Number> fieldsMap = new HashMap<String, Number>() {
            {
                put("utilizedProcUnits", metrics.systemUtil.sample.lparsUtil.processor.utilizedProcUnits);
                put("maxVirtualProcessors", metrics.systemUtil.sample.lparsUtil.processor.maxVirtualProcessors);
                put("currentVirtualProcessors", metrics.systemUtil.sample.lparsUtil.processor.currentVirtualProcessors);
                //donatedProcUnits: metrics.systemUtil.utilSamples.first().lparsUtil.first().processor.donatedProcUnits.first(),
                put("entitledProcUnits", metrics.systemUtil.sample.lparsUtil.processor.entitledProcUnits);
                //idleProcUnits: metrics.systemUtil.utilSamples.first().lparsUtil.first().processor.idleProcUnits.first(),
                //maxProcUnits: metrics.systemUtil.utilSamples.first().lparsUtil.first().processor.maxProcUnits.first(),
                put("utilizedCappedProcUnits", metrics.systemUtil.sample.lparsUtil.processor.utilizedCappedProcUnits);
                put("utilizedUncappedProcUnits", metrics.systemUtil.sample.lparsUtil.processor.utilizedUncappedProcUnits);
                put("timePerInstructionExecution", metrics.systemUtil.sample.lparsUtil.processor.timeSpentWaitingForDispatch);
                put("timeSpentWaitingForDispatch", metrics.systemUtil.sample.lparsUtil.processor.timePerInstructionExecution);
            }
        };

        //map.put("fields", fieldsMap)
        log.debug("getProcessorMetrics() - fields: " + fieldsMap.toString());

        Measurement measurement = new Measurement(tagsMap, fieldsMap);
        list.add(measurement);

        return list;
    }

    //@CompileDynamic
    List<Measurement> getVirtualEthernetAdapterMetrics() {

        List<Measurement> list = new ArrayList<>();
        metrics.systemUtil.sample.lparsUtil.network.virtualEthernetAdapters.forEach( adapter -> {

            HashMap<String, String> tagsMap = new HashMap<String, String>() {
                {
                    put("system", system.name);
                    put("partition", name);
                    put("sea", adapter.sharedEthernetAdapterId);
                    put("viosId", adapter.viosId.toString());
                    put("vlanId", adapter.vlanId.toString());
                    put("vswitchId", adapter.vswitchId.toString());
                }
            };
            log.debug("getVirtualEthernetAdapterMetrics() - tags: " + tagsMap.toString());

            HashMap<String, Number> fieldsMap = new HashMap<String, Number>() {
                {
                    put("receivedPhysicalBytes", adapter.receivedPhysicalBytes);
                    put("sentPhysicalBytes", adapter.sentPhysicalBytes);
                    put("receivedBytes", adapter.receivedBytes);
                    put("sentBytes", adapter.sentBytes);
                }
            };
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
            //Map<String, Map> map = new HashMap<String, Map>()

            HashMap<String, String> tagsMap = new HashMap<String, String>() {
                {
                    put("system", system.name);
                    put("partition", name);
                    put("viosId", adapter.viosId.toString());
                    put("wwpn", adapter.wwpn);
                }
            };

            //map.put("tags", tagsMap)
            log.debug("getVirtualFiberChannelAdaptersMetrics() - tags: " + tagsMap.toString());

            HashMap<String, Number> fieldsMap = new HashMap<String, Number>() {
                {
                    put("transmittedBytes", adapter.transmittedBytes.get(0));
                    put("writeBytes", adapter.writeBytes.get(0));
                    put("readBytes", adapter.readBytes.get(0));
                }
            };

            //map.put("fields", fieldsMap)
            log.debug("getVirtualFiberChannelAdaptersMetrics() - fields: " + fieldsMap.toString());

            Measurement measurement = new Measurement(tagsMap, fieldsMap);
            list.add(measurement);
        });

        return list;
    }

}
