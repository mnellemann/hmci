package biz.nellemann.hmci


import biz.nellemann.hmci.pojo.PcmData
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

@Slf4j
class ManagedSystem {

    public String id
    public String name
    public String type
    public String model
    public String serialNumber
    public Map<String, LogicalPartition> partitions = new HashMap<String, LogicalPartition>()

    protected PcmData metrics

    ManagedSystem(String id) {
        this.id = id
    }

    String toString() {
        return "[${id}] ${name} (${type}-${model} ${serialNumber})"
    }




    void processPcmJson(String json) {
        log.debug("processPcmJson()")
        def jsonObject = new JsonSlurper().parseText(json)
        String systemUuid = jsonObject?.systemUtil?.utilInfo?.uuid as String
        if(systemUuid && this.id == systemUuid) {
            log.debug("processPcmJson() - Found UUID for this ManagedSystem: " + systemUuid)
            processMetrics(json)
        }
    }

    private void processMetrics(String json) {
        //metrics = new JsonSlurper().parseText(json) as PcmData
        def pcmMap = new JsonSlurper().parseText(json)
        metrics = new PcmData(pcmMap as Map)
    }

}
