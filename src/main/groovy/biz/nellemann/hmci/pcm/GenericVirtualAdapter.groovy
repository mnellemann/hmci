package biz.nellemann.hmci.pcm

import groovy.transform.ToString

@ToString
class GenericVirtualAdapter {

    String id
    String type
    Integer viosId
    String physicalLocation
    List<BigDecimal> numOfReads
    List<BigDecimal> numOfWrites
    List<BigDecimal> readBytes
    List<BigDecimal> writeBytes
    List<BigDecimal> transmittedBytes

}
