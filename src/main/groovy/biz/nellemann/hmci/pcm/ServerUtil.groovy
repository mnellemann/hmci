package biz.nellemann.hmci.pcm

import groovy.transform.ToString

@ToString
class ServerUtil {

    ServerProcessor processor
    ServerMemory memory
    PhysicalProcessorPool physicalProcessorPool
    List<SharedProcessorPool> sharedProcessorPool

}
