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

    void processMetrics(String json) {
        //metrics = new JsonSlurper().parseText(json) as PcmData
        def pcmMap = new JsonSlurper().parseText(json)
        metrics = new PcmData(pcmMap as Map)
    }

}
