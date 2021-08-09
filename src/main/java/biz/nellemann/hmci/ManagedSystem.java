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

    //public final String hmcId;
    public final String id;
    public final String name;
    public final String type;
    public final String model;
    public final String serialNumber;

    public final SystemEnergy energy;


    ManagedSystem(String id, String name, String type, String model, String serialNumber) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.model = model;
        this.serialNumber = serialNumber;
        this.energy = new SystemEnergy(this);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (%s-%s %s)", id, name, type, model, serialNumber);
    }


    List<Measurement> getDetails() {

        List<Measurement> list = new ArrayList<>();

        HashMap<String, String> tagsMap = new HashMap<>();
        tagsMap.put("servername", name);
        log.trace("getDetails() - tags: " + tagsMap.toString());

        Map<String, Object> fieldsMap = new HashMap<>();
        fieldsMap.put("mtm", String.format("%s-%s %s", type, model, serialNumber));
        fieldsMap.put("APIversion", metrics.systemUtil.utilInfo.version);
        fieldsMap.put("metric", metrics.systemUtil.utilInfo.metricType);
        fieldsMap.put("frequency", metrics.systemUtil.utilInfo.frequency);
        fieldsMap.put("nextract", "HMCi");
        fieldsMap.put("name", name);
        fieldsMap.put("utilizedProcUnits", metrics.systemUtil.sample.systemFirmwareUtil.utilizedProcUnits);
        fieldsMap.put("assignedMem", metrics.systemUtil.sample.systemFirmwareUtil.assignedMem);
        log.trace("getDetails() - fields: " + fieldsMap.toString());

        list.add(new Measurement(tagsMap, fieldsMap));
        return list;
    }


    // Memory
    List<Measurement> getMemoryMetrics() {

        List<Measurement> list = new ArrayList<>();

        HashMap<String, String> tagsMap = new HashMap<>();
        tagsMap.put("servername", name);
        log.trace("getMemoryMetrics() - tags: " + tagsMap.toString());

        Map<String, Object> fieldsMap = new HashMap<>();
        fieldsMap.put("totalMem", metrics.systemUtil.sample.serverUtil.memory.totalMem);
        fieldsMap.put("availableMem", metrics.systemUtil.sample.serverUtil.memory.availableMem);
        fieldsMap.put("configurableMem", metrics.systemUtil.sample.serverUtil.memory.configurableMem);
        fieldsMap.put("assignedMemToLpars", metrics.systemUtil.sample.serverUtil.memory.assignedMemToLpars);
        fieldsMap.put("virtualPersistentMem", metrics.systemUtil.sample.serverUtil.memory.virtualPersistentMem);
        log.trace("getMemoryMetrics() - fields: " + fieldsMap.toString());

        list.add(new Measurement(tagsMap, fieldsMap));
        return list;
    }


    // Processor
    List<Measurement> getProcessorMetrics() {

        List<Measurement> list = new ArrayList<>();

        HashMap<String, String> tagsMap = new HashMap<>();
        tagsMap.put("servername", name);
        log.trace("getProcessorMetrics() - tags: " + tagsMap.toString());

        HashMap<String, Object> fieldsMap = new HashMap<>();
        fieldsMap.put("totalProcUnits", metrics.systemUtil.sample.serverUtil.processor.totalProcUnits);
        fieldsMap.put("utilizedProcUnits", metrics.systemUtil.sample.serverUtil.processor.utilizedProcUnits);
        fieldsMap.put("availableProcUnits", metrics.systemUtil.sample.serverUtil.processor.availableProcUnits);
        fieldsMap.put("configurableProcUnits", metrics.systemUtil.sample.serverUtil.processor.configurableProcUnits);
        log.trace("getProcessorMetrics() - fields: " + fieldsMap.toString());

        list.add(new Measurement(tagsMap, fieldsMap));
        return list;
    }

    // Shared ProcessorPools
    List<Measurement> getSharedProcessorPools() {

        List<Measurement> list = new ArrayList<>();
        metrics.systemUtil.sample.serverUtil.sharedProcessorPool.forEach(sharedProcessorPool -> {

            HashMap<String, String> tagsMap = new HashMap<>();
            tagsMap.put("servername", name);
            tagsMap.put("pool", sharedProcessorPool.id);
            tagsMap.put("poolname", sharedProcessorPool.name);
            log.trace("getSharedProcessorPools() - tags: " + tagsMap.toString());

            HashMap<String, Object> fieldsMap = new HashMap<>();
            fieldsMap.put("assignedProcUnits", sharedProcessorPool.assignedProcUnits);
            fieldsMap.put("availableProcUnits", sharedProcessorPool.availableProcUnits);
            fieldsMap.put("utilizedProcUnits", sharedProcessorPool.utilizedProcUnits);
            fieldsMap.put("borrowedProcUnits", sharedProcessorPool.borrowedProcUnits);
            fieldsMap.put("configuredProcUnits", sharedProcessorPool.configuredProcUnits);
            log.trace("getSharedProcessorPools() - fields: " + fieldsMap.toString());

            list.add(new Measurement(tagsMap, fieldsMap));
        });

        return list;
    }


    // Physical ProcessorPool
    List<Measurement> getPhysicalProcessorPool() {

        List<Measurement> list = new ArrayList<>();

        HashMap<String, String> tagsMap = new HashMap<>();
        tagsMap.put("servername", name);
        log.trace("getPhysicalProcessorPool() - tags: " + tagsMap.toString());

        HashMap<String, Object> fieldsMap = new HashMap<>();
        fieldsMap.put("assignedProcUnits", metrics.systemUtil.sample.serverUtil.physicalProcessorPool.assignedProcUnits);
        fieldsMap.put("availableProcUnits", metrics.systemUtil.sample.serverUtil.physicalProcessorPool.availableProcUnits);
        fieldsMap.put("utilizedProcUnits", metrics.systemUtil.sample.serverUtil.physicalProcessorPool.utilizedProcUnits);
        fieldsMap.put("configuredProcUnits", metrics.systemUtil.sample.serverUtil.physicalProcessorPool.configuredProcUnits);
        fieldsMap.put("borrowedProcUnits", metrics.systemUtil.sample.serverUtil.physicalProcessorPool.borrowedProcUnits);
        log.trace("getPhysicalProcessorPool() - fields: " + fieldsMap.toString());

        list.add(new Measurement(tagsMap, fieldsMap));
        return list;
    }


    // VIO Details
    List<Measurement> getViosDetails() {

        List<Measurement> list = new ArrayList<>();
        metrics.systemUtil.sample.viosUtil.forEach(vios -> {

            HashMap<String, String> tagsMap = new HashMap<>();
            tagsMap.put("servername", name);
            tagsMap.put("viosname", vios.name);
            log.trace("getViosDetails() - tags: " + tagsMap.toString());

            HashMap<String, Object> fieldsMap = new HashMap<>();
            fieldsMap.put("viosid", vios.id);
            fieldsMap.put("viosstate", vios.state);
            fieldsMap.put("viosname", vios.name);
            fieldsMap.put("affinityScore", vios.affinityScore);
            log.trace("getViosDetails() - fields: " + fieldsMap.toString());

            list.add(new Measurement(tagsMap, fieldsMap));
        });

        return list;
    }


    // VIO Memory
    List<Measurement> getViosMemoryMetrics() {

        List<Measurement> list = new ArrayList<>();
        metrics.systemUtil.sample.viosUtil.forEach(vios -> {

                HashMap<String, String> tagsMap = new HashMap<>();
                tagsMap.put("servername", name);
                tagsMap.put("viosname", vios.name);
                log.trace("getViosMemoryMetrics() - tags: " + tagsMap.toString());

                HashMap<String, Object> fieldsMap = new HashMap<>();
                Number assignedMem = getNumberMetricObject(vios.memory.assignedMem);
                Number utilizedMem = getNumberMetricObject(vios.memory.utilizedMem);
                if(assignedMem != null) {
                    fieldsMap.put("assignedMem", vios.memory.assignedMem);
                }
                if(utilizedMem != null) {
                    fieldsMap.put("utilizedMem", vios.memory.utilizedMem);
                }
                if(assignedMem != null && utilizedMem != null) {
                    Number usedMemPct = (utilizedMem.intValue() * 100 ) / assignedMem.intValue();
                    fieldsMap.put("utilizedPct", usedMemPct.floatValue());
                }
                log.trace("getViosMemoryMetrics() - fields: " + fieldsMap.toString());

                list.add(new Measurement(tagsMap, fieldsMap));
            });

        return list;
    }


    // VIO Processor
    List<Measurement> getViosProcessorMetrics() {

        List<Measurement> list = new ArrayList<>();
        metrics.systemUtil.sample.viosUtil.forEach(vios -> {

            HashMap<String, String> tagsMap = new HashMap<>();
            tagsMap.put("servername", name);
            tagsMap.put("viosname", vios.name);
            log.trace("getViosProcessorMetrics() - tags: " + tagsMap.toString());

            HashMap<String, Object> fieldsMap = new HashMap<>();
            fieldsMap.put("utilizedProcUnits", vios.processor.utilizedProcUnits);
            fieldsMap.put("utilizedCappedProcUnits", vios.processor.utilizedCappedProcUnits);
            fieldsMap.put("utilizedUncappedProcUnits", vios.processor.utilizedUncappedProcUnits);
            fieldsMap.put("currentVirtualProcessors", vios.processor.currentVirtualProcessors);
            fieldsMap.put("maxVirtualProcessors", vios.processor.maxVirtualProcessors);
            fieldsMap.put("maxProcUnits", vios.processor.maxProcUnits);
            fieldsMap.put("entitledProcUnits", vios.processor.entitledProcUnits);
            fieldsMap.put("donatedProcUnits", vios.processor.donatedProcUnits);
            fieldsMap.put("idleProcUnits", vios.processor.idleProcUnits);
            fieldsMap.put("timeSpentWaitingForDispatch", vios.processor.timePerInstructionExecution);
            fieldsMap.put("timePerInstructionExecution", vios.processor.timeSpentWaitingForDispatch);
            fieldsMap.put("weight", vios.processor.weight);
            log.trace("getViosProcessorMetrics() - fields: " + fieldsMap.toString());

            list.add(new Measurement(tagsMap, fieldsMap));
        });

        return list;
    }


    // VIOs - Network
    List<Measurement> getViosNetworkLpars() {

        List<Measurement> list = new ArrayList<>();
        metrics.systemUtil.sample.viosUtil.forEach(vios -> {

            HashMap<String, String> tagsMap = new HashMap<>();
            tagsMap.put("servername", name);
            tagsMap.put("viosname", vios.name);
            log.trace("getViosNetworkLpars() - tags: " + tagsMap.toString());

            HashMap<String, Object> fieldsMap = new HashMap<>();
            fieldsMap.put("clientlpars", vios.network.clientLpars.size());
            log.trace("getViosNetworkLpars() - fields: " + fieldsMap.toString());

            list.add(new Measurement(tagsMap, fieldsMap));
        });

        return list;
    }


    // VIO Network - Shared
    List<Measurement> getViosNetworkSharedAdapters() {

        List<Measurement> list = new ArrayList<>();
        metrics.systemUtil.sample.viosUtil.forEach(vios -> {

            vios.network.sharedAdapters.forEach(adapter -> {

                HashMap<String, String> tagsMap = new HashMap<>();
                tagsMap.put("servername", name);
                tagsMap.put("viosname", vios.name);
                //tagsMap.put("id", adapter.id);
                tagsMap.put("location", adapter.physicalLocation);
                log.trace("getViosNetworkSharedAdapters() - tags: " + tagsMap.toString());

                HashMap<String, Object> fieldsMap = new HashMap<>();
                fieldsMap.put("id", adapter.id);
                fieldsMap.put("type", adapter.type);
                fieldsMap.put("sentBytes", adapter.sentBytes);
                fieldsMap.put("sentPackets", adapter.sentPackets);
                fieldsMap.put("receivedBytes", adapter.receivedBytes);
                fieldsMap.put("receivedPackets", adapter.receivedPackets);
                fieldsMap.put("droppedPackets", adapter.droppedPackets);
                fieldsMap.put("transferredBytes", adapter.transferredBytes);
                fieldsMap.put("physicalLocation", adapter.physicalLocation);
                log.trace("getViosNetworkSharedAdapters() - fields: " + fieldsMap.toString());

                list.add(new Measurement(tagsMap, fieldsMap));
            });

        });

        return list;
    }


    // VIO Network - Virtual
    List<Measurement> getViosNetworkVirtualAdapters() {

        List<Measurement> list = new ArrayList<>();

        metrics.systemUtil.sample.viosUtil.forEach( vios -> {

            vios.network.virtualEthernetAdapters.forEach( adapter -> {

                HashMap<String, String> tagsMap = new HashMap<>();
                tagsMap.put("vlanid", String.valueOf(adapter.vlanId));
                tagsMap.put("vswitchid", String.valueOf(adapter.vswitchId));
                tagsMap.put("systemname", name);
                tagsMap.put("viosname", vios.name);
                tagsMap.put("location", adapter.physicalLocation);
                log.trace("getViosNetworkVirtualAdapters() - tags: " + tagsMap.toString());

                HashMap<String, Object> fieldsMap = new HashMap<>();
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
                fieldsMap.put("physicalLocation", adapter.physicalLocation);
                log.trace("getViosNetworkVirtualAdapters() - fields: " + fieldsMap.toString());

                list.add(new Measurement(tagsMap, fieldsMap));
            });

        });

        return list;
    }


    // VIO Network - Generic
    List<Measurement> getViosNetworkGenericAdapters() {

        List<Measurement> list = new ArrayList<>();

        metrics.systemUtil.sample.viosUtil.forEach( vios -> {

            vios.network.genericAdapters.forEach( adapter -> {

                HashMap<String, String> tagsMap = new HashMap<>();
                tagsMap.put("id", adapter.id);
                tagsMap.put("servername", name);
                tagsMap.put("viosname", vios.name);
                tagsMap.put("location", adapter.physicalLocation);
                log.trace("getViosNetworkGenericAdapters() - tags: " + tagsMap.toString());

                HashMap<String, Object> fieldsMap = new HashMap<>();
                fieldsMap.put("sentBytes", adapter.sentBytes);
                fieldsMap.put("sentPackets", adapter.sentPackets);
                fieldsMap.put("receivedBytes", adapter.receivedBytes);
                fieldsMap.put("receivedPackets", adapter.receivedPackets);
                fieldsMap.put("droppedPackets", adapter.droppedPackets);
                fieldsMap.put("transferredBytes", adapter.transferredBytes);
                log.trace("getViosNetworkGenericAdapters() - fields: " + fieldsMap.toString());

                list.add(new Measurement(tagsMap, fieldsMap));
            });

        });

        return list;
    }

    // VIOs - Storage
    List<Measurement> getViosStorageLpars() {

        List<Measurement> list = new ArrayList<>();
        metrics.systemUtil.sample.viosUtil.forEach(vios -> {

            HashMap<String, String> tagsMap = new HashMap<>();
            tagsMap.put("servername", name);
            tagsMap.put("viosname", vios.name);
            log.trace("getViosStorageLpars() - tags: " + tagsMap.toString());

            HashMap<String, Object> fieldsMap = new HashMap<>();
            fieldsMap.put("clientlpars", vios.storage.clientLpars.size());
            log.trace("getViosStorageLpars() - fields: " + fieldsMap.toString());

            list.add(new Measurement(tagsMap, fieldsMap));
        });

        return list;
    }

    // VIO Storage FC
    List<Measurement> getViosStorageFiberChannelAdapters() {

        List<Measurement> list = new ArrayList<>();
        metrics.systemUtil.sample.viosUtil.forEach( vios -> {
            log.trace("getViosStorageFiberChannelAdapters() - VIOS: " + vios.name);

            vios.storage.fiberChannelAdapters.forEach( adapter -> {

                HashMap<String, String> tagsMap = new HashMap<>();
                tagsMap.put("id", adapter.id);
                tagsMap.put("servername", name);
                tagsMap.put("viosname", vios.name);
                tagsMap.put("location", adapter.physicalLocation);
                log.trace("getViosStorageFiberChannelAdapters() - tags: " + tagsMap.toString());

                HashMap<String, Object> fieldsMap = new HashMap<>();
                fieldsMap.put("numOfReads", adapter.numOfReads);
                fieldsMap.put("numOfWrites", adapter.numOfWrites);
                fieldsMap.put("readBytes", adapter.readBytes);
                fieldsMap.put("writeBytes", adapter.writeBytes);
                fieldsMap.put("transmittedBytes", adapter.transmittedBytes);
                fieldsMap.put("physicalLocation", adapter.physicalLocation);
                log.trace("getViosStorageFiberChannelAdapters() - fields: " + fieldsMap.toString());

                list.add(new Measurement(tagsMap, fieldsMap));
            });

        });

        return list;
    }

    // VIO Storage SSP TODO
    List<Measurement> getViosStorageSharedStoragePools() {

        List<Measurement> list = new ArrayList<>();
/*
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
*/
        return list;
    }

    // VIO Storage - Physical
    List<Measurement> getViosStoragePhysicalAdapters() {

        List<Measurement> list = new ArrayList<>();
        metrics.systemUtil.sample.viosUtil.forEach( vios -> {
            log.trace("getViosStoragePhysicalAdapters() - VIOS: " + vios.name);

            vios.storage.genericPhysicalAdapters.forEach( adapter -> {

                HashMap<String, String> tagsMap = new HashMap<>();
                tagsMap.put("servername", name);
                tagsMap.put("viosname", vios.name);
                tagsMap.put("id", adapter.id);
                tagsMap.put("location", adapter.physicalLocation);
                log.trace("getViosStoragePhysicalAdapters() - tags: " + tagsMap.toString());

                HashMap<String, Object> fieldsMap = new HashMap<>();
                fieldsMap.put("numOfReads", adapter.numOfReads);
                fieldsMap.put("numOfWrites", adapter.numOfWrites);
                fieldsMap.put("readBytes", adapter.readBytes);
                fieldsMap.put("writeBytes", adapter.writeBytes);
                fieldsMap.put("transmittedBytes", adapter.transmittedBytes);
                fieldsMap.put("type", adapter.type);
                fieldsMap.put("physicalLocation", adapter.physicalLocation);
                log.trace("getViosStoragePhysicalAdapters() - fields: " + fieldsMap.toString());

                list.add(new Measurement(tagsMap, fieldsMap));
            });

        });

        return list;
    }


    // VIO Storage - Virtual
    List<Measurement> getViosStorageVirtualAdapters() {

        List<Measurement> list = new ArrayList<>();
        metrics.systemUtil.sample.viosUtil.forEach( vios -> {
            log.trace("getViosStorageVirtualAdapters() - VIOS: " + vios.name);

            vios.storage.genericVirtualAdapters.forEach( adapter -> {

                HashMap<String, String> tagsMap = new HashMap<>();
                tagsMap.put("servername", name);
                tagsMap.put("viosname", vios.name);
                tagsMap.put("location", adapter.physicalLocation);
                tagsMap.put("id", adapter.id);
                log.trace("getViosStorageVirtualAdapters() - tags: " + tagsMap.toString());

                HashMap<String, Object> fieldsMap = new HashMap<>();
                fieldsMap.put("numOfReads", adapter.numOfReads);
                fieldsMap.put("numOfWrites", adapter.numOfWrites);
                fieldsMap.put("readBytes", adapter.readBytes);
                fieldsMap.put("writeBytes", adapter.writeBytes);
                fieldsMap.put("transmittedBytes", adapter.transmittedBytes);
                fieldsMap.put("type", adapter.type);
                fieldsMap.put("physicalLocation", adapter.physicalLocation);
                log.trace("getViosStorageVirtualAdapters() - fields: " + fieldsMap.toString());

                list.add(new Measurement(tagsMap, fieldsMap));
            });

        });

        return list;
    }
}
