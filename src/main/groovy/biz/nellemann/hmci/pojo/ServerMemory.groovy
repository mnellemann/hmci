package biz.nellemann.hmci.pojo

import groovy.transform.ToString

@ToString
class ServerMemory {
    List<BigDecimal> totalMem
    List<BigDecimal> availableMem
    List<BigDecimal> configurableMem
    List<BigDecimal> assignedMemToLpars
}
