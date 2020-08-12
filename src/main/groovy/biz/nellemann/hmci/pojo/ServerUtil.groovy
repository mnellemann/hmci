package biz.nellemann.hmci.pojo

import groovy.transform.ToString

@ToString
class ServerUtil {

    ServerProcessor processor
    ServerMemory memory
    PhysicalProcessorPool physicalProcessorPool
    List<SharedProcessorPool> sharedProcessorPool

}
