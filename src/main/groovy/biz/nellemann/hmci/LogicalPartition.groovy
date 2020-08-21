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
package biz.nellemann.hmci

import groovy.util.logging.Slf4j

@Slf4j
class LogicalPartition extends MetaSystem {

    public String id
    public String name
    public String type
    ManagedSystem system

    LogicalPartition(String id, String name, String type, ManagedSystem system) {
        this.id = id
        this.name = name
        this.type = type
        this.system = system
    }

    String toString() {
        return "[${id}] ${name} (${type})"
    }


    List<Map> getMemoryMetrics() {

        List<Map> list = new ArrayList<>()
        Map<String, Map> map = new HashMap<String, Map>()

        HashMap<String, String> tagsMap = [
                system: system.name,
                partition: name,
        ]
        map.put("tags", tagsMap)
        log.debug("getMemoryMetrics() - tags: " + tagsMap.toString())

        HashMap<String, BigDecimal> fieldsMap = [
                logicalMem: metrics.systemUtil?.utilSamples?.first()?.lparsUtil?.first()?.memory?.logicalMem?.first(),
                backedPhysicalMem: metrics.systemUtil?.utilSamples?.first()?.lparsUtil?.first()?.memory?.backedPhysicalMem?.first(),
        ]
        map.put("fields", fieldsMap)
        log.debug("getMemoryMetrics() - fields: " + fieldsMap.toString())

        list.add(map)
        return list
    }


    List<Map> getProcessorMetrics() {

        List<Map> list = new ArrayList<>()
        Map<String, Map> map = new HashMap<String, Map>()

        HashMap<String, String> tagsMap = [
                system: system.name,
                partition: name,
        ]
        map.put("tags", tagsMap)
        log.debug("getProcessorMetrics() - tags: " + tagsMap.toString())

        HashMap<String, BigDecimal> fieldsMap = [
            utilizedProcUnits: metrics.systemUtil?.utilSamples?.first()?.lparsUtil?.first()?.processor?.utilizedProcUnits?.first(),
            //maxVirtualProcessors: metrics.systemUtil.utilSamples.first().lparsUtil.first().processor.maxVirtualProcessors.first(),
            //currentVirtualProcessors: metrics.systemUtil.utilSamples.first().lparsUtil.first().processor.currentVirtualProcessors.first(),
            //donatedProcUnits: metrics.systemUtil.utilSamples.first().lparsUtil.first().processor.donatedProcUnits.first(),
            //entitledProcUnits: metrics.systemUtil.utilSamples.first().lparsUtil.first().processor.entitledProcUnits.first(),
            //idleProcUnits: metrics.systemUtil.utilSamples.first().lparsUtil.first().processor.idleProcUnits.first(),
            //maxProcUnits: metrics.systemUtil.utilSamples.first().lparsUtil.first().processor.maxProcUnits.first(),
            utilizedCappedProcUnits: metrics.systemUtil?.utilSamples?.first()?.lparsUtil?.first()?.processor?.utilizedCappedProcUnits?.first(),
            utilizedUncappedProcUnits: metrics.systemUtil?.utilSamples?.first()?.lparsUtil?.first()?.processor?.utilizedUncappedProcUnits?.first(),
            timePerInstructionExecution: metrics.systemUtil?.utilSamples?.first()?.lparsUtil?.first()?.processor?.timeSpentWaitingForDispatch?.first(),
            timeSpentWaitingForDispatch: metrics.systemUtil?.utilSamples?.first()?.lparsUtil?.first()?.processor?.timePerInstructionExecution?.first(),
        ]
        map.put("fields", fieldsMap)
        log.debug("getProcessorMetrics() - fields: " + fieldsMap.toString())

        list.add(map)
        return list
    }


    List<Map> getVirtualEthernetAdapterMetrics() {

        List<Map> list = new ArrayList<>()
        Map<String, Map> map = new HashMap<String, Map>()
        metrics.systemUtil?.utilSamples?.first()?.lparsUtil?.first()?.network?.virtualEthernetAdapters?.each {

            HashMap<String, String> tagsMap = [
                    system: system.name,
                    partition: name,
                    sea: it.sharedEthernetAdapterId as String,
                    viosId: it.viosId as String,
                    vlanId: it.vlanId as String,
                    vswitchId: it.vswitchId as String,
            ]
            map.put("tags", tagsMap)
            log.debug("getVirtualEthernetAdapterMetrics() - tags: " + tagsMap.toString())

            HashMap<String, BigDecimal> fieldsMap = [
                    receivedPhysicalBytes: it.receivedPhysicalBytes.first(),
                    sentPhysicalBytes: it.sentPhysicalBytes.first(),
                    receivedBytes: it.receivedBytes.first(),
                    sentBytes: it.sentBytes.first(),
            ]
            map.put("fields", fieldsMap)
            log.debug("getVirtualEthernetAdapterMetrics() - fields: " + fieldsMap.toString())

            list.add(map)
        }

        return list
    }


    //PartitionVirtualFiberChannelAdapters
    List<Map> getVirtualFiberChannelAdaptersMetrics() {

        List<Map> list = new ArrayList<>()
        Map<String, Map> map = new HashMap<String, Map>()
        metrics.systemUtil?.utilSamples?.first()?.lparsUtil?.first()?.storage?.virtualFiberChannelAdapters?.each {

            HashMap<String, String> tagsMap = [
                    system: system.name,
                    partition: name,
                    viosId: it.viosId as String,
                    wwpn: it.wwpn,
            ]
            map.put("tags", tagsMap)
            log.debug("getVirtualFiberChannelAdaptersMetrics() - tags: " + tagsMap.toString())

            HashMap<String, BigDecimal> fieldsMap = [
                    transmittedBytes: it.transmittedBytes.first(),
                    writeBytes: it.writeBytes.first(),
            ]
            map.put("fields", fieldsMap)
            log.debug("getVirtualFiberChannelAdaptersMetrics() - fields: " + fieldsMap.toString())

            list.add(map)
        }

        return list
    }
}
