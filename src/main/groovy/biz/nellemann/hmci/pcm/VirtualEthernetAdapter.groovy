package biz.nellemann.hmci.pcm

import groovy.transform.ToString

@ToString
class VirtualEthernetAdapter {

    String physicalLocation
    Integer vlanId
    Integer vswitchId
    Boolean isPortVlanId
    Integer viosId
    String sharedEthernetAdapterId
    List<BigDecimal> receivedPackets
    List<BigDecimal> sentPackets
    List<BigDecimal> droppedPackets
    List<BigDecimal> sentBytes
    List<BigDecimal> receivedBytes
    List<BigDecimal> receivedPhysicalPackets
    List<BigDecimal> sentPhysicalPackets
    List<BigDecimal> droppedPhysicalPackets
    List<BigDecimal> sentPhysicalBytes
    List<BigDecimal> receivedPhysicalBytes
    List<BigDecimal> transferredBytes
    List<BigDecimal> transferredPhysicalBytes

}
