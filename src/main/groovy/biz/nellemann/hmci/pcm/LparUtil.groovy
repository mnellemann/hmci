package biz.nellemann.hmci.pcm

import groovy.transform.ToString

@ToString
class LparUtil {

    Integer id
    String uuid
    String name
    String state
    String type
    String osType
    Integer affinityScore

    LparMemory memory
    LparProcessor processor
    Network network
    Storage storage

}
