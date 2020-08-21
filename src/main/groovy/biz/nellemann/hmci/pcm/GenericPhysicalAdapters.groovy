package biz.nellemann.hmci.pcm

import groovy.transform.ToString

@ToString
class GenericPhysicalAdapters {

    String id
    String type
    String physicalLocation
    List<BigDecimal> numOfReads
    List<BigDecimal> numOfWrites
    List<BigDecimal> readBytes
    List<BigDecimal> writeBytes
    List<BigDecimal> transmittedBytes

}
