package biz.nellemann.hmci.pcm

import groovy.transform.ToString

@ToString
class SharedProcessorPool {

    String id
    String name
    List<BigDecimal> assignedProcUnits
    List<BigDecimal> utilizedProcUnits
    List<BigDecimal> availableProcUnits
    List<BigDecimal> configuredProcUnits
    List<BigDecimal> borrowedProcUnits

}
