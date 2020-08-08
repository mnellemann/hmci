package biz.nellemann.hmci.pojo

import groovy.transform.ToString

@ToString
class PhysicalProcessorPool {

    List<BigDecimal> assignedProcUnits
    List<BigDecimal> utilizedProcUnits
    List<BigDecimal> availableProcUnits
    List<BigDecimal> configuredProcUnits
    List<BigDecimal> borrowedProcUnits

}
