package biz.nellemann.hmci.pcm

import groovy.transform.ToString

@ToString
class GenericAdapter {

    String id
    String type
    String physicalLocation
    List<BigDecimal> receivedPackets
    List<BigDecimal> sentPackets
    List<BigDecimal> droppedPackets
    List<BigDecimal> sentBytes
    List<BigDecimal> receivedBytes
    List<BigDecimal> transferredBytes

}
