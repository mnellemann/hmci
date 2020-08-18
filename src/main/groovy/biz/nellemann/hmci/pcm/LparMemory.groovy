package biz.nellemann.hmci.pcm

import groovy.transform.ToString

@ToString
class LparMemory {

    List<BigDecimal> logicalMem
    List<BigDecimal> backedPhysicalMem

}
