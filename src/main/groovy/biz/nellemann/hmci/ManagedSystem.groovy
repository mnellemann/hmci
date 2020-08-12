package biz.nellemann.hmci

import groovy.util.logging.Slf4j


@Slf4j
class ManagedSystem extends MetaSystem {

    public String id
    public String name
    public String type
    public String model
    public String serialNumber

    // From PCM Data


    ManagedSystem(String id, String name, String type, String model, String serialNumber) {
        this.id = id
        this.name = name
        this.type = type
        this.model = model
        this.serialNumber = serialNumber
    }

    String toString() {
        return "[${id}] ${name} (${type}-${model} ${serialNumber})"
    }

    
    Object getMetrics(String metric) {
        switch (metric) {
            case "SystemSharedProcessorPool":
                return getSharedProcessorPools()
                break
            
        }
    }

    Map<String,BigDecimal> getMemoryMetrics() {

        HashMap<String, BigDecimal> map = [
            totalMem: metrics.systemUtil.utilSamples.first().serverUtil.memory.totalMem.first(),
            availableMem: metrics.systemUtil.utilSamples.first().serverUtil.memory.availableMem.first(),
            configurableMem: metrics.systemUtil.utilSamples.first().serverUtil.memory.configurableMem.first(),
            assignedMemToLpars: metrics.systemUtil.utilSamples.first().serverUtil.memory.assignedMemToLpars.first(),
        ]

        return map
    }


    Map<String,BigDecimal> getProcessorMetrics() {

        HashMap<String, BigDecimal> map = [
            availableProcUnits: metrics.systemUtil.utilSamples.first().serverUtil.processor.totalProcUnits.first(),
            utilizedProcUnits: metrics.systemUtil.utilSamples.first().serverUtil.processor.utilizedProcUnits.first(),
            availableProcUnits: metrics.systemUtil.utilSamples.first().serverUtil.processor.availableProcUnits.first(),
            configurableProcUnits: metrics.systemUtil.utilSamples.first().serverUtil.processor.configurableProcUnits.first(),
        ]

        return map
    }

    Map<String, BigDecimal> getPhysicalProcessorPool() {

        HashMap<String, BigDecimal> map = [
            assignedProcUnits: metrics.systemUtil.utilSamples.first().serverUtil.physicalProcessorPool.assignedProcUnits.first(),
            availableProcUnits: metrics.systemUtil.utilSamples.first().serverUtil.physicalProcessorPool.availableProcUnits.first(),
        ]

        return map
    }
    
    

    Map<String, Map<String, BigDecimal>> getSharedProcessorPools() {

        Map<String, Map> map = new HashMap<String, Map>()
        metrics.systemUtil.utilSamples.first().serverUtil.sharedProcessorPool.each {

            HashMap<String, BigDecimal> innerMap = [
                    assignedProcUnits: it.assignedProcUnits.first(),
                    availableProcUnits: it.availableProcUnits.first(),
            ]
            map.put(it.name, innerMap)
        }
        return map
    }


    // SystemSharedAdapters
    // SystemGenericPhysicalAdapters
    // SystemGenericVirtualAdapters
    // SystemGenericPhysicalAdapters
    // SystemGenericAdapters
    // SystemFiberChannelAdapters

}
