package biz.nellemann.hmci

import groovy.util.logging.Slf4j


@Slf4j
class ManagedSystem extends MetaSystem {

    public final String hmcId
    public final String id
    public final String name
    public final String type
    public final String model
    public final String serialNumber


    ManagedSystem(String hmcId, String id, String name, String type, String model, String serialNumber) {
        this.hmcId = hmcId
        this.id = id
        this.name = name
        this.type = type
        this.model = model
        this.serialNumber = serialNumber
    }

    String toString() {
        return "[${id}] ${name} (${type}-${model} ${serialNumber})"
    }


    List<Map> getMemoryMetrics() {

        List<Map> list = new ArrayList<>()
        Map<String, Map> map = new HashMap<String, Map>()

        HashMap<String, String> tagsMap = [
                system: name,
        ]
        map.put("tags", tagsMap)
        log.debug("getMemoryMetrics() - tags: " + tagsMap.toString())

        HashMap<String, BigDecimal> fieldsMap = [
            totalMem: metrics.systemUtil.utilSamples.first().serverUtil.memory.totalMem.first(),
            availableMem: metrics.systemUtil.utilSamples.first().serverUtil.memory.availableMem.first(),
            configurableMem: metrics.systemUtil.utilSamples.first().serverUtil.memory.configurableMem.first(),
            assignedMemToLpars: metrics.systemUtil.utilSamples.first().serverUtil.memory.assignedMemToLpars.first(),
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
                system: name,
        ]
        map.put("tags", tagsMap)
        log.debug("getProcessorMetrics() - tags: " + tagsMap.toString())

        HashMap<String, BigDecimal> fieldsMap = [
            availableProcUnits: metrics.systemUtil.utilSamples.first().serverUtil.processor.totalProcUnits.first(),
            utilizedProcUnits: metrics.systemUtil.utilSamples.first().serverUtil.processor.utilizedProcUnits.first(),
            availableProcUnits: metrics.systemUtil.utilSamples.first().serverUtil.processor.availableProcUnits.first(),
            configurableProcUnits: metrics.systemUtil.utilSamples.first().serverUtil.processor.configurableProcUnits.first(),
        ]
        map.put("fields", fieldsMap)
        log.debug("getProcessorMetrics() - fields: " + fieldsMap.toString())

        list.add(map)
        return list
    }


    List<Map> getSharedProcessorPools() {

        List<Map> list = new ArrayList<>()
        Map<String, Map> map = new HashMap<String, Map>()
        metrics.systemUtil.utilSamples.first().serverUtil.sharedProcessorPool.each {

            HashMap<String, String> tagsMap = [
                    system: name,
                    pool: it.name,
            ]
            map.put("tags", tagsMap)
            log.debug("getSharedProcessorPools() - tags: " + tagsMap.toString())

            HashMap<String, BigDecimal> fieldsMap = [
                    assignedProcUnits: it.assignedProcUnits.first(),
                    availableProcUnits: it.availableProcUnits.first(),
            ]
            map.put("fields", fieldsMap)
            log.debug("getSharedProcessorPools() - fields: " + fieldsMap.toString())

            list.add(map)

        }

        return list
    }


    List<Map> getSystemSharedAdapters() {

        List<Map> list = new ArrayList<>()
        Map<String, Map> map = new HashMap<String, Map>()
        metrics.systemUtil.utilSamples.first().viosUtil.each {vios ->

            vios.network.sharedAdapters.each {

                HashMap<String, String> tagsMap = [
                        system: name,
                        type: it.type,
                        vios: vios.name,
                ]
                map.put("tags", tagsMap)
                log.debug("getSystemSharedAdapters() - tags: " + tagsMap.toString())

                HashMap<String, BigDecimal> fieldsMap = [
                        sentBytes: it.sentBytes.first(),
                        receivedBytes: it.receivedBytes.first(),
                        transferredBytes: it.transferredBytes.first(),
                ]
                map.put("fields", fieldsMap)
                log.debug("getSystemSharedAdapters() - fields: " + fieldsMap.toString())

                list.add(map)
            }

        }

        return list
    }


    List<Map> getSystemFiberChannelAdapters() {

        List<Map> list = new ArrayList<>()
        Map<String, Map> map = new HashMap<String, Map>()
        metrics.systemUtil.utilSamples.first().viosUtil.each { vios ->
            vios.storage.fiberChannelAdapters.each {

                HashMap<String, String> tagsMap = [
                        system: name,
                        wwpn: it.wwpn,
                        vios: vios.name,
                        device: it.physicalLocation,
                ]
                map.put("tags", tagsMap)
                log.debug("getSystemFiberChannelAdapters() - tags: " + tagsMap.toString())

                HashMap<String, BigDecimal> fieldsMap = [
                        writeBytes: it.writeBytes.first(),
                        readBytes: it.readBytes.first(),
                        transmittedBytes: it.transmittedBytes.first(),
                ]
                map.put("fields", fieldsMap)
                log.debug("getSystemFiberChannelAdapters() - fields: " + fieldsMap.toString())

                list.add(map)

            }

        }

        return list

    }

}
