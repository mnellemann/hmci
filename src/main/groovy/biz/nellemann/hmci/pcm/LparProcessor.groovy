package biz.nellemann.hmci.pcm

import groovy.transform.ToString

@ToString
class LparProcessor {

    Integer poolId
    Integer weight
    String mode
    List<BigDecimal> maxVirtualProcessors
    List<BigDecimal> currentVirtualProcessors
    List<BigDecimal> maxProcUnits
    List<BigDecimal> entitledProcUnits
    List<BigDecimal> utilizedProcUnits
    List<BigDecimal> utilizedCappedProcUnits
    List<BigDecimal> utilizedUncappedProcUnits
    List<BigDecimal> idleProcUnits
    List<BigDecimal> donatedProcUnits
    List<BigDecimal> timeSpentWaitingForDispatch
    List<BigDecimal> timePerInstructionExecution

}
