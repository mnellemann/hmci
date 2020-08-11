package biz.nellemann.hmci

import spock.lang.Specification

class LogicalPartitionTest extends Specification {


    void "test processPcmJson for LogicalPartition"() {

        setup:
        def testFile = new File(getClass().getResource('/pcm-data-logical-partition.json').toURI())
        def testJson = testFile.getText('UTF-8')

        when:
        LogicalPartition lpar = new LogicalPartition("2DE05DB6-8AD5-448F-8327-0F488D287E82", "e09834d1-c930-3883-bdad-405d8e26e166", "9Flash01", "OS400")
        lpar.processMetrics(testJson)

        then:
        lpar.metrics.systemUtil.utilSamples.first().lparsUtil.first().memory.logicalMem.first() == 8192.000
        lpar.metrics.systemUtil.utilSamples.first().lparsUtil.first().processor.utilizedProcUnits.first() == 0.001
        lpar.metrics.systemUtil.utilSamples.first().lparsUtil.first().network.virtualEthernetAdapters.first().receivedPackets.first() == 3.867
    }


}
