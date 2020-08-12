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


    Map<String,BigDecimal> getMemoryMetrics() {

        HashMap<String, BigDecimal> map = [
                logicalMem: metrics.systemUtil.utilSamples.first().lparsUtil.first().memory.logicalMem.first(),
                backedPhysicalMem: metrics.systemUtil.utilSamples.first().lparsUtil.first().memory.backedPhysicalMem.first(),
        ]

        return map
    }


    Map<String,BigDecimal> getProcessorMetrics() {

        HashMap<String, BigDecimal> map = [
                utilizedProcUnits: metrics.systemUtil.utilSamples.first().lparsUtil.first().processor.utilizedProcUnits.first(),
                maxVirtualProcessors: metrics.systemUtil.utilSamples.first().lparsUtil.first().processor.maxVirtualProcessors.first(),
                currentVirtualProcessors: metrics.systemUtil.utilSamples.first().lparsUtil.first().processor.currentVirtualProcessors.first(),
                donatedProcUnits: metrics.systemUtil.utilSamples.first().lparsUtil.first().processor.donatedProcUnits.first(),
                entitledProcUnits: metrics.systemUtil.utilSamples.first().lparsUtil.first().processor.entitledProcUnits.first(),
                idleProcUnits: metrics.systemUtil.utilSamples.first().lparsUtil.first().processor.idleProcUnits.first(),
                maxProcUnits: metrics.systemUtil.utilSamples.first().lparsUtil.first().processor.maxProcUnits.first(),
                utilizedCappedProcUnits: metrics.systemUtil.utilSamples.first().lparsUtil.first().processor.utilizedCappedProcUnits.first(),
                utilizedUncappedProcUnits: metrics.systemUtil.utilSamples.first().lparsUtil.first().processor.utilizedUncappedProcUnits.first(),
                timePerInstructionExecution: metrics.systemUtil.utilSamples.first().lparsUtil.first().processor.timeSpentWaitingForDispatch.first(),
                timeSpentWaitingForDispatch: metrics.systemUtil.utilSamples.first().lparsUtil.first().processor.timePerInstructionExecution.first(),
        ]

        return map
    }


    // PartitionVSCSIAdapters - VIOS?

    // PartitionVirtualEthernetAdapters
    // PartitionVirtualFiberChannelAdapters


    List<Map> getVirtualEthernetAdapterMetrics() {

        List<Map> list = new ArrayList<>()
        Map<String, Map> map = new HashMap<String, Map>()
        metrics.systemUtil.utilSamples.first().lparsUtil.first().network?.virtualEthernetAdapters?.each {

            HashMap<String, String> tagsMap = [
                    system: system.name,
                    partition: name,
                    sea: it.sharedEthernetAdapterId,
                    viosId: it.viosId,
                    vlanId: it.vlanId,
                    vswitchId: it.vswitchId,
            ]
            map.put("tags", tagsMap)

            HashMap<String, BigDecimal> fieldsMap = [
                    receivedPhysicalBytes: it.receivedPhysicalBytes.first(),
                    sentPhysicalBytes: it.sentPhysicalBytes.first(),
                    receivedBytes: it.receivedBytes.first(),
                    sentBytes: it.sentBytes.first(),
            ]
            map.put(it.physicalLocation, fieldsMap)

            list.add(map)
        }
        
        return list
    }

}
