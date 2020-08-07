package biz.nellemann.hmci

import groovy.util.logging.Slf4j

@Slf4j
class LogicalPartition {

    public String id
    public String name
    public String type

    protected List<String> pcmLinks

    LogicalPartition(String id) {
        this.id = id
    }

    String toString() {
        return "[${id}] ${name} (${type})"
    }

    void processMetrics() {
        log.info("processMetrics() - TODO: Store metrics here.")
    }
}
