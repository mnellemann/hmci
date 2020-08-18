package biz.nellemann.hmci.pcm

import groovy.transform.ToString

@ToString
class ServerProcessor {

    List<BigDecimal> totalProcUnits
    List<BigDecimal> utilizedProcUnits
    List<BigDecimal> availableProcUnits
    List<BigDecimal> configurableProcUnits

}
