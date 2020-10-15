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

class ManagedSystem extends MetaSystem {

    private final static Logger log = LoggerFactory.getLogger(ManagedSystem.class);

    public final String hmcId;
    public final String id;
    public final String name;
    public final String type;
    public final String model;
    public final String serialNumber;


    ManagedSystem(String hmcId, String id, String name, String type, String model, String serialNumber) {
        this.hmcId = hmcId;
        this.id = id;
        this.name = name;
        this.type = type;
        this.model = model;
        this.serialNumber = serialNumber;
    }

    public String toString() {
        return String.format("[%s] %s (%s-%s %s)", id, name, type, model, serialNumber);
    }


    List<Measurement> getMemoryMetrics() {

        List<Measurement> list = new ArrayList<>();
        //Map<String, Map> map = new HashMap<String, Map>()

        HashMap<String, String> tagsMap = new HashMap<String, String>() {
            {
                put("system", name);
            }
        };

        //map.put("tags", tagsMap)
        log.debug("getMemoryMetrics() - tags: " + tagsMap.toString());

        Map<String, Number> fieldsMap = new HashMap<String, Number>() {
            {
                put("totalMem", metrics.systemUtil.sample.serverUtil.memory.totalMem);
                put("availableMem", metrics.systemUtil.sample.serverUtil.memory.availableMem);
                put("configurableMem", metrics.systemUtil.sample.serverUtil.memory.configurableMem);
                put("assignedMemToLpars", metrics.systemUtil.sample.serverUtil.memory.assignedMemToLpars);
            }
        };

        //map.put("fields", fieldsMap)
        log.debug("getMemoryMetrics() - fields: " + fieldsMap.toString());

        Measurement measurement = new Measurement(tagsMap, fieldsMap);
        list.add(measurement);

        return list;
    }


    List<Measurement> getProcessorMetrics() {

        List<Measurement> list = new ArrayList<>();
        //Map<String, Map> map = new HashMap<>()

        HashMap<String, String> tagsMap = new HashMap<String, String>() {
            {
                put("system", name);
            }
        };

        //map.put("tags", tagsMap)
        //measurement.tags = tagsMap;
        log.debug("getProcessorMetrics() - tags: " + tagsMap.toString());

        HashMap<String, Number> fieldsMap = new HashMap<String, Number>() {
            {
                put("totalProcUnits", metrics.systemUtil.sample.serverUtil.processor.totalProcUnits);
                put("utilizedProcUnits", metrics.systemUtil.sample.serverUtil.processor.utilizedProcUnits);
                put("availableProcUnits", metrics.systemUtil.sample.serverUtil.processor.availableProcUnits);
                put("configurableProcUnits", metrics.systemUtil.sample.serverUtil.processor.configurableProcUnits);
            }
        };
        //map.put("fields", fieldsMap)
        //measurement.fields = fieldsMap;
        log.debug("getProcessorMetrics() - fields: " + fieldsMap.toString());

        Measurement measurement = new Measurement(tagsMap, fieldsMap);
        list.add(measurement);

        return list;
    }


    List<Measurement> getSharedProcessorPools() {

        List<Measurement> list = new ArrayList<>();
        metrics.systemUtil.sample.serverUtil.sharedProcessorPool.forEach(adapter -> {
            //Map<String, Map> map = new HashMap<String, Map>()

            HashMap<String, String> tagsMap = new HashMap<String, String>() {
                {
                    put("system", name);
                    put("pool", adapter.name);
                }
            };

            //map.put("tags", tagsMap)
            log.debug("getSharedProcessorPools() - tags: " + tagsMap.toString());

            HashMap<String, Number> fieldsMap = new HashMap<String, Number>() {
                {
                    put("assignedProcUnits", adapter.assignedProcUnits);
                    put("availableProcUnits", adapter.availableProcUnits);
                }
            };

            //map.put("fields", fieldsMap)
            log.debug("getSharedProcessorPools() - fields: " + fieldsMap.toString());

            Measurement measurement = new Measurement(tagsMap, fieldsMap);
            list.add(measurement);
        });

        return list;
    }


    List<Measurement> getSystemSharedAdapters() {

        List<Measurement> list = new ArrayList<>();
        metrics.systemUtil.sample.viosUtil.forEach(vios -> {

            vios.network.sharedAdapters.forEach(adapter -> {
                //Map<String, Map> map = new HashMap<String, Map>()
                Measurement measurement = new Measurement();

                HashMap<String, String> tagsMap = new HashMap<String, String>() {
                    {
                        put("system", name);
                        put("type", adapter.type);
                        put("vios", vios.name);
                    }
                };

                //map.put("tags", tagsMap)
                measurement.tags = tagsMap;
                log.debug("getSystemSharedAdapters() - tags: " + tagsMap.toString());

                HashMap<String, Number> fieldsMap = new HashMap<String, Number>() {
                    {
                        put("sentBytes", adapter.sentBytes);
                        put("receivedBytes", adapter.receivedBytes);
                        put("transferredBytes", adapter.transferredBytes);
                    }
                };
                //map.put("fields", fieldsMap)
                measurement.fields = fieldsMap;
                log.debug("getSystemSharedAdapters() - fields: " + fieldsMap.toString());

                list.add(measurement);
            });

        });

        return list;
    }


    List<Measurement> getSystemFiberChannelAdapters() {

        List<Measurement> list = new ArrayList<>();
        metrics.systemUtil.sample.viosUtil.forEach( vios -> {
            log.debug("getSystemFiberChannelAdapters() - VIOS: " + vios.name);

            vios.storage.fiberChannelAdapters.forEach( adapter -> {
                //HashMap<String, Map> map = new HashMap<>()
                Measurement measurement = new Measurement();

                HashMap<String, String> tagsMap = new HashMap<String, String>() {
                    {
                        put("id", adapter.id);
                        put("system", name);
                        put("wwpn", adapter.wwpn);
                        put("vios", vios.name);
                        put("device", adapter.physicalLocation);
                    }
                };

                //map.put("tags", tagsMap)
                measurement.tags = tagsMap;
                log.debug("getSystemFiberChannelAdapters() - tags: " + tagsMap.toString());

                HashMap<String, Number> fieldsMap = new HashMap<String, Number>() {
                    {
                        put("writeBytes", adapter.writeBytes);
                        put("readBytes", adapter.readBytes);
                        put("transmittedBytes", adapter.transmittedBytes);
                    }
                };
                //map.put("fields", fieldsMap)
                measurement.fields = fieldsMap;
                log.debug("getSystemFiberChannelAdapters() - fields: " + fieldsMap.toString());

                list.add(measurement);
            });

        });

        return list;
    }


    List<Measurement> getSystemGenericPhysicalAdapters() {

        List<Measurement> list = new ArrayList<>();

        metrics.systemUtil.sample.viosUtil.forEach( vios -> {

            vios.storage.genericPhysicalAdapters.forEach( adapter -> {

                Measurement measurement = new Measurement();

                HashMap<String, String> tagsMap = new HashMap<String, String>() {
                    {
                        put("id", adapter.id);
                        put("system", name);
                        put("vios", vios.name);
                        put("device", adapter.physicalLocation);
                    }
                };

                measurement.tags = tagsMap;
                log.debug("getSystemGenericPhysicalAdapters() - tags: " + tagsMap.toString());

                HashMap<String, Number> fieldsMap = new HashMap<String, Number>() {
                    {
                        put("writeBytes", adapter.writeBytes);
                        put("readBytes", adapter.readBytes);
                        put("transmittedBytes", adapter.transmittedBytes);
                    }
                };

                measurement.fields = fieldsMap;
                log.debug("getSystemGenericPhysicalAdapters() - fields: " + fieldsMap.toString());

                list.add(measurement);
            });

        });

        return list;
    }


    List<Measurement> getSystemGenericVirtualAdapters() {

        List<Measurement> list = new ArrayList<>();

        metrics.systemUtil.sample.viosUtil.forEach( vios -> {

            vios.storage.genericVirtualAdapters.forEach( adapter -> {

                Measurement measurement = new Measurement();

                HashMap<String, String> tagsMap = new HashMap<String, String>() {
                    {
                        put("id", adapter.id);
                        put("system", name);
                        put("vios", vios.name);
                        put("device", adapter.physicalLocation);
                    }
                };

                measurement.tags = tagsMap;
                log.debug("getSystemGenericVirtualAdapters() - tags: " + tagsMap.toString());

                HashMap<String, Number> fieldsMap = new HashMap<String, Number>() {
                    {
                        put("writeBytes", adapter.writeBytes);
                        put("readBytes", adapter.readBytes);
                        put("transmittedBytes", adapter.transmittedBytes);
                    }
                };

                measurement.fields = fieldsMap;
                log.debug("getSystemGenericVirtualAdapters() - fields: " + fieldsMap.toString());

                list.add(measurement);
            });

        });

        return list;
    }

}
