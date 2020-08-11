package biz.nellemann.hmci

import groovy.util.logging.Slf4j


@Slf4j
class ManagedSystem extends MetaSystem {

    public String id
    public String name
    public String type
    public String model
    public String serialNumber


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


    Map<String,BigDecimal> getMemoryMetrics() {

        HashMap<String, BigDecimal> map = [
                totalMem: metrics.systemUtil.utilSamples.first().serverUtil.memory.totalMem.first(),
                availableMem: metrics.systemUtil.utilSamples.first().serverUtil.memory.availableMem.first(),
                configurableMem: metrics.systemUtil.utilSamples.first().serverUtil.memory.configurableMem.first(),
                assignedMemToLpars: metrics.systemUtil.utilSamples.first().serverUtil.memory.assignedMemToLpars.first()
        ]

        return map
    }


    Map<String,BigDecimal> getProcessorMetrics() {

        HashMap<String, BigDecimal> map = [
                availableProcUnits: metrics.systemUtil.utilSamples.first().serverUtil.processor.availableProcUnits.first(),
        ]

        return map
    }

}
