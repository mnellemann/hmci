package biz.nellemann.hmci.pcm

import groovy.transform.ToString

@ToString
class ServerMemory {

    List<BigDecimal> totalMem
    List<BigDecimal> availableMem
    List<BigDecimal> configurableMem
    List<BigDecimal> assignedMemToLpars

}
