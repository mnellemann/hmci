package biz.nellemann.hmci.pojo

import groovy.transform.ToString

@ToString
class VirtualFiberChannelAdapter {

    String wwpn
    String wwpn2
    String physicalLocation
    String physicalPortWWPN
    Integer viosId
    BigDecimal numOfReads
    BigDecimal numOfWrites
    BigDecimal readBytes
    BigDecimal writeBytes
    BigDecimal runningSpeed
    BigDecimal transmittedBytes

}
