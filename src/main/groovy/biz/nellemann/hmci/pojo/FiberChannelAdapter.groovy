package biz.nellemann.hmci.pojo

import groovy.transform.ToString

@ToString
class FiberChannelAdapter {

    String id
    String wwpn
    String physicalLocation
    Integer numOfPorts
    List<BigDecimal> numOfReads
    List<BigDecimal> numOfWrites
    List<BigDecimal> readBytes
    List<BigDecimal> writeBytes
    List<BigDecimal> runningSpeed
    List<BigDecimal> transmittedBytes

}
