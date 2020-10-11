package biz.nellemann.hmci

import spock.lang.Specification

class LogicalPartitionTest extends Specification {


    void "test processPcmJson for LogicalPartition"() {

        setup:
        def testFile = new File(getClass().getResource('/pcm-data-logical-partition.json').toURI())
        def testJson = testFile.getText('UTF-8')

        when:
        ManagedSystem system = new ManagedSystem("site1", "e09834d1-c930-3883-bdad-405d8e26e166", "Test Name","Test Type", "Test Model", "Test S/N")
        LogicalPartition lpar = new LogicalPartition("2DE05DB6-8AD5-448F-8327-0F488D287E82", "9Flash01", "OS400", system)
        lpar.processMetrics(testJson)

        then:
        lpar.metrics.systemUtil.sample.lparsUtil.memory.logicalMem == 8192.000
        lpar.metrics.systemUtil.sample.lparsUtil.processor.utilizedProcUnits == 0.001
        lpar.metrics.systemUtil.sample.lparsUtil.network.virtualEthernetAdapters.first().receivedBytes == 276.467

    }


    void "test getMemoryMetrics"() {

        setup:
        def testFile = new File(getClass().getResource('/pcm-data-logical-partition.json').toURI())
        def testJson = testFile.getText('UTF-8')
        ManagedSystem system = new ManagedSystem("site1", "e09834d1-c930-3883-bdad-405d8e26e166", "Test Name","Test Type", "Test Model", "Test S/N")
        LogicalPartition lpar = new LogicalPartition("2DE05DB6-8AD5-448F-8327-0F488D287E82", "9Flash01", "OS400", system)

        when:
        lpar.processMetrics(testJson)
        List<Measurement> listOfMeasurements = lpar.getMemoryMetrics()

        then:
        listOfMeasurements.size() == 1
        listOfMeasurements.first().fields['logicalMem'] == 8192.000
        listOfMeasurements.first().tags['partition'] == '9Flash01'

    }

    void "test getProcessorMetrics"() {

        setup:
        def testFile = new File(getClass().getResource('/pcm-data-logical-partition.json').toURI())
        def testJson = testFile.getText('UTF-8')
        ManagedSystem system = new ManagedSystem("site1", "e09834d1-c930-3883-bdad-405d8e26e166", "Test Name","Test Type", "Test Model", "Test S/N")
        LogicalPartition lpar = new LogicalPartition("2DE05DB6-8AD5-448F-8327-0F488D287E82", "9Flash01", "OS400", system)

        when:
        lpar.processMetrics(testJson)
        List<Measurement> listOfMeasurements = lpar.getProcessorMetrics()

        then:
        listOfMeasurements.size() == 1
        listOfMeasurements.first().fields['utilizedProcUnits'] == 0.001
        listOfMeasurements.first().tags['partition'] == '9Flash01'

    }

    void "test getVirtualEthernetAdapterMetrics"() {

        setup:
        def testFile = new File(getClass().getResource('/pcm-data-logical-partition.json').toURI())
        def testJson = testFile.getText('UTF-8')
        ManagedSystem system = new ManagedSystem("site1", "e09834d1-c930-3883-bdad-405d8e26e166", "Test Name","Test Type", "Test Model", "Test S/N")
        LogicalPartition lpar = new LogicalPartition("2DE05DB6-8AD5-448F-8327-0F488D287E82", "9Flash01", "OS400", system)

        when:
        lpar.processMetrics(testJson)
        List<Measurement> listOfMeasurements = lpar.getVirtualEthernetAdapterMetrics()

        then:
        listOfMeasurements.size() == 1
        listOfMeasurements.first().fields['receivedBytes'] == 276.467
        listOfMeasurements.first().tags['sea'] == 'ent5'
    }

    void "test getVirtualFiberChannelAdaptersMetrics"() {

        setup:
        def testFile = new File(getClass().getResource('/pcm-data-logical-partition.json').toURI())
        def testJson = testFile.getText('UTF-8')
        ManagedSystem system = new ManagedSystem("site1", "e09834d1-c930-3883-bdad-405d8e26e166", "Test Name","Test Type", "Test Model", "Test S/N")
        LogicalPartition lpar = new LogicalPartition("2DE05DB6-8AD5-448F-8327-0F488D287E82", "9Flash01", "OS400", system)

        when:
        lpar.processMetrics(testJson)
        List<Measurement> listOfMeasurements = lpar.getVirtualFiberChannelAdaptersMetrics()

        then:
        listOfMeasurements.size() == 4
        listOfMeasurements.first().fields['writeBytes'] == 6690.133
        listOfMeasurements.first().tags['viosId'] == '1'

    }

}
