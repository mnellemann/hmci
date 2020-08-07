package biz.nellemann.hmci.pojo

import groovy.transform.ToString

@ToString
class LparMemory {

    List<BigDecimal> logicalMem
    List<BigDecimal> backedPhysicalMem

}
