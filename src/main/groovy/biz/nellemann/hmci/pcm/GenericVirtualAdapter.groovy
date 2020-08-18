package biz.nellemann.hmci.pcm

import groovy.transform.ToString

@ToString
class GenericVirtualAdapter {

    String id
    String type
    Integer viosId
    String physicalLocation
    BigDecimal numOfReads
    BigDecimal numOfWrites
    BigDecimal readBytes
    BigDecimal writeBytes
    BigDecimal transmittedBytes

}
