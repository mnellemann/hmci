package biz.nellemann.hmci

import groovy.util.logging.Slf4j

@Slf4j
class LogicalPartition extends MetaSystem {

    public String id
    public String name
    public String type
    public String systemId

    LogicalPartition(String id, String systemId, String name, String type) {
        this.id = id
        this.systemId = systemId
        this.name = name
        this.type = type
    }

    String toString() {
        return "[${id}] ${name} (${type})"
    }

}
