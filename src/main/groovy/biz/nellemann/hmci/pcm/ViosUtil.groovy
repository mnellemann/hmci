package biz.nellemann.hmci.pcm

import groovy.transform.ToString

@ToString
class ViosUtil {

    String id
    String uuid
    String name
    String state
    Integer affinityScore

    Memory memory
    LparProcessor processor
    Network network
    Storage storage

    class Memory {
        List<BigDecimal> assignedMem
        List<BigDecimal> utilizedMem
    }

}
