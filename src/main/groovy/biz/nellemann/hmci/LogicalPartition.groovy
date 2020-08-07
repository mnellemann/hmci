package biz.nellemann.hmci


import biz.nellemann.hmci.pojo.SystemUtil
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j

@Slf4j
class LogicalPartition {

    public String id
    public String name
    public String type

    protected SystemUtil metrics

    LogicalPartition(String id) {
        this.id = id
    }

    String toString() {
        return "[${id}] ${name} (${type})"
    }

    void processMetrics(String json) {
        def pcmMap = new JsonSlurper().parseText(json)
        metrics = new SystemUtil(pcmMap as Map)
    }

}
