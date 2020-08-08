package biz.nellemann.hmci.pojo

import groovy.transform.ToString

@ToString
class SharedAdapter {

    String id
    String type
    String physicalLocation
    List<BigDecimal> receivedPackets
    List<BigDecimal> sentPackets
    List<BigDecimal> droppedPackets
    List<BigDecimal> sentBytes
    List<BigDecimal> receivedBytes
    List<BigDecimal> transferredBytes
    List<String> bridgedAdapters

}
