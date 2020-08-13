package biz.nellemann.hmci.pojo

import groovy.transform.ToString

@ToString
class VirtualFiberChannelAdapter {

    String wwpn
    String wwpn2
    String physicalLocation
    String physicalPortWWPN
    Integer viosId
    List<BigDecimal> numOfReads
    List<BigDecimal> numOfWrites
    List<BigDecimal> readBytes
    List<BigDecimal> writeBytes
    List<BigDecimal> runningSpeed
    List<BigDecimal> transmittedBytes

}
