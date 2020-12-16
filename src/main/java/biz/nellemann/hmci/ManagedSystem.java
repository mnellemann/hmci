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

    public final SystemEnergy energy;


    ManagedSystem(String hmcId, String id, String name, String type, String model, String serialNumber) {
        this.hmcId = hmcId;
        this.id = id;
        this.name = name;
        this.type = type;
        this.model = model;
        this.serialNumber = serialNumber;
        this.energy = new SystemEnergy(this);
    }

    public String toString() {
        return String.format("[%s] %s (%s-%s %s)", id, name, type, model, serialNumber);
    }


    List<Measurement> getMemoryMetrics() {

        List<Measurement> list = new ArrayList<>();

        HashMap<String, String> tagsMap = new HashMap<String, String>();
        tagsMap.put("system", name);
        log.debug("getMemoryMetrics() - tags: " + tagsMap.toString());

        Map<String, Number> fieldsMap = new HashMap<String, Number>();
        fieldsMap.put("totalMem", metrics.systemUtil.sample.serverUtil.memory.totalMem);
        fieldsMap.put("availableMem", metrics.systemUtil.sample.serverUtil.memory.availableMem);
        fieldsMap.put("configurableMem", metrics.systemUtil.sample.serverUtil.memory.configurableMem);
        fieldsMap.put("assignedMemToLpars", metrics.systemUtil.sample.serverUtil.memory.assignedMemToLpars);
        log.debug("getMemoryMetrics() - fields: " + fieldsMap.toString());

        list.add(new Measurement(tagsMap, fieldsMap));

        return list;
    }


    List<Measurement> getProcessorMetrics() {

        List<Measurement> list = new ArrayList<>();

        HashMap<String, String> tagsMap = new HashMap<String, String>();
        tagsMap.put("system", name);
        log.debug("getProcessorMetrics() - tags: " + tagsMap.toString());

        HashMap<String, Number> fieldsMap = new HashMap<String, Number>();
        fieldsMap.put("totalProcUnits", metrics.systemUtil.sample.serverUtil.processor.totalProcUnits);
        fieldsMap.put("utilizedProcUnits", metrics.systemUtil.sample.serverUtil.processor.utilizedProcUnits);
        fieldsMap.put("availableProcUnits", metrics.systemUtil.sample.serverUtil.processor.availableProcUnits);
        fieldsMap.put("configurableProcUnits", metrics.systemUtil.sample.serverUtil.processor.configurableProcUnits);
        log.debug("getProcessorMetrics() - fields: " + fieldsMap.toString());

        list.add(new Measurement(tagsMap, fieldsMap));
        return list;
    }


    List<Measurement> getSharedProcessorPools() {

        List<Measurement> list = new ArrayList<>();
        metrics.systemUtil.sample.serverUtil.sharedProcessorPool.forEach(adapter -> {

            HashMap<String, String> tagsMap = new HashMap<String, String>();
            tagsMap.put("system", name);
            tagsMap.put("pool", adapter.name);
            log.debug("getSharedProcessorPools() - tags: " + tagsMap.toString());

            HashMap<String, Number> fieldsMap = new HashMap<String, Number>();
            fieldsMap.put("assignedProcUnits", adapter.assignedProcUnits);
            fieldsMap.put("availableProcUnits", adapter.availableProcUnits);
            log.debug("getSharedProcessorPools() - fields: " + fieldsMap.toString());

            list.add(new Measurement(tagsMap, fieldsMap));
        });

        return list;
    }


    // VIOs Memory
    List<Measurement> getViosMemoryMetrics() {

        List<Measurement> list = new ArrayList<>();
        metrics.systemUtil.sample.viosUtil.forEach(vios -> {

                HashMap<String, String> tagsMap = new HashMap<String, String>();
                tagsMap.put("system", name);
                tagsMap.put("vios", vios.name);
                log.debug("getViosMemoryMetrics() - tags: " + tagsMap.toString());

                HashMap<String, Number> fieldsMap = new HashMap<String, Number>();
                fieldsMap.put("assignedMem", vios.memory.assignedMem);
                fieldsMap.put("utilizedMem", vios.memory.utilizedMem);
                log.debug("getViosMemoryMetrics() - fields: " + fieldsMap.toString());

                list.add(new Measurement(tagsMap, fieldsMap));
            });

        return list;
    }


    // VIOs Processor
    List<Measurement> getViosProcessorMetrics() {

        List<Measurement> list = new ArrayList<>();
        metrics.systemUtil.sample.viosUtil.forEach(vios -> {

            HashMap<String, String> tagsMap = new HashMap<String, String>();
            tagsMap.put("system", name);
            tagsMap.put("vios", vios.name);
            log.debug("getViosProcessorMetrics() - tags: " + tagsMap.toString());

            HashMap<String, Number> fieldsMap = new HashMap<String, Number>();
            fieldsMap.put("utilizedProcUnits", vios.processor.utilizedProcUnits);
            fieldsMap.put("maxVirtualProcessors", vios.processor.maxVirtualProcessors);
            fieldsMap.put("currentVirtualProcessors", vios.processor.currentVirtualProcessors);
            fieldsMap.put("entitledProcUnits", vios.processor.entitledProcUnits);
            fieldsMap.put("utilizedCappedProcUnits", vios.processor.utilizedCappedProcUnits);
            fieldsMap.put("utilizedUncappedProcUnits", vios.processor.utilizedUncappedProcUnits);
            fieldsMap.put("timePerInstructionExecution", vios.processor.timeSpentWaitingForDispatch);
            fieldsMap.put("timeSpentWaitingForDispatch", vios.processor.timePerInstructionExecution);
            log.debug("getViosProcessorMetrics() - fields: " + fieldsMap.toString());

            list.add(new Measurement(tagsMap, fieldsMap));
        });

        return list;
    }


    // VIOs
    List<Measurement> getSystemSharedAdapters() {

        List<Measurement> list = new ArrayList<>();
        metrics.systemUtil.sample.viosUtil.forEach(vios -> {

            vios.network.sharedAdapters.forEach(adapter -> {

                HashMap<String, String> tagsMap = new HashMap<String, String>();
                tagsMap.put("system", name);
                tagsMap.put("type", adapter.type);
                tagsMap.put("vios", vios.name);
                tagsMap.put("device", adapter.physicalLocation);
                log.debug("getSystemSharedAdapters() - tags: " + tagsMap.toString());

                HashMap<String, Number> fieldsMap = new HashMap<String, Number>();
                fieldsMap.put("sentBytes", adapter.sentBytes);
                fieldsMap.put("receivedBytes", adapter.receivedBytes);
                fieldsMap.put("transferredBytes", adapter.transferredBytes);
                log.debug("getSystemSharedAdapters() - fields: " + fieldsMap.toString());

                list.add(new Measurement(tagsMap, fieldsMap));
            });

        });

        return list;
    }

    // VIOs
    List<Measurement> getSystemFiberChannelAdapters() {

        List<Measurement> list = new ArrayList<>();
        metrics.systemUtil.sample.viosUtil.forEach( vios -> {
            log.debug("getSystemFiberChannelAdapters() - VIOS: " + vios.name);

            vios.storage.fiberChannelAdapters.forEach( adapter -> {

                HashMap<String, String> tagsMap = new HashMap<String, String>();
                tagsMap.put("id", adapter.id);
                tagsMap.put("system", name);
                tagsMap.put("wwpn", adapter.wwpn);
                tagsMap.put("vios", vios.name);
                tagsMap.put("device", adapter.physicalLocation);
                log.debug("getSystemFiberChannelAdapters() - tags: " + tagsMap.toString());

                HashMap<String, Number> fieldsMap = new HashMap<String, Number>();
                fieldsMap.put("writeBytes", adapter.writeBytes);
                fieldsMap.put("readBytes", adapter.readBytes);
                fieldsMap.put("transmittedBytes", adapter.transmittedBytes);
                log.debug("getSystemFiberChannelAdapters() - fields: " + fieldsMap.toString());

                list.add(new Measurement(tagsMap, fieldsMap));
            });

        });

        return list;
    }


    // VIOs
    /*
    List<Measurement> getSystemGenericPhysicalAdapters() {

        List<Measurement> list = new ArrayList<>();

        metrics.systemUtil.sample.viosUtil.forEach( vios -> {

            vios.storage.genericPhysicalAdapters.forEach( adapter -> {

                HashMap<String, String> tagsMap = new HashMap<String, String>();
                tagsMap.put("id", adapter.id);
                tagsMap.put("system", name);
                tagsMap.put("vios", vios.name);
                tagsMap.put("device", adapter.physicalLocation);
                log.debug("getSystemGenericPhysicalAdapters() - tags: " + tagsMap.toString());

                HashMap<String, Number> fieldsMap = new HashMap<String, Number>();
                fieldsMap.put("writeBytes", adapter.writeBytes);
                fieldsMap.put("readBytes", adapter.readBytes);
                fieldsMap.put("transmittedBytes", adapter.transmittedBytes);
                log.debug("getSystemGenericPhysicalAdapters() - fields: " + fieldsMap.toString());

                list.add(new Measurement(tagsMap, fieldsMap));
            });

        });

        return list;
    }
     */


    // VIOs
    /*
    List<Measurement> getSystemGenericVirtualAdapters() {

        List<Measurement> list = new ArrayList<>();

        metrics.systemUtil.sample.viosUtil.forEach( vios -> {

            vios.storage.genericVirtualAdapters.forEach( adapter -> {

                HashMap<String, String> tagsMap = new HashMap<String, String>();
                tagsMap.put("id", adapter.id);
                tagsMap.put("system", name);
                tagsMap.put("vios", vios.name);
                tagsMap.put("device", adapter.physicalLocation);
                log.debug("getSystemGenericVirtualAdapters() - tags: " + tagsMap.toString());

                HashMap<String, Number> fieldsMap = new HashMap<String, Number>();
                fieldsMap.put("writeBytes", adapter.writeBytes);
                fieldsMap.put("readBytes", adapter.readBytes);
                fieldsMap.put("transmittedBytes", adapter.transmittedBytes);
                log.debug("getSystemGenericVirtualAdapters() - fields: " + fieldsMap.toString());

                list.add(new Measurement(tagsMap, fieldsMap));
            });

        });

        return list;
    }
     */

    // VIOs
    List<Measurement> getSystemVirtualEthernetAdapters() {

        List<Measurement> list = new ArrayList<>();

        metrics.systemUtil.sample.viosUtil.forEach( vios -> {

            vios.network.virtualEthernetAdapters.forEach( adapter -> {

                HashMap<String, String> tagsMap = new HashMap<String, String>();
                tagsMap.put("system", name);
                tagsMap.put("vios", vios.name);
                tagsMap.put("device", adapter.physicalLocation);
                log.debug("getSystemGenericVirtualAdapters() - tags: " + tagsMap.toString());

                HashMap<String, Number> fieldsMap = new HashMap<String, Number>();
                fieldsMap.put("sentBytes", adapter.sentBytes);
                fieldsMap.put("receivedBytes", adapter.receivedBytes);
                log.debug("getSystemGenericVirtualAdapters() - fields: " + fieldsMap.toString());

                list.add(new Measurement(tagsMap, fieldsMap));
            });

        });

        return list;
    }

}
