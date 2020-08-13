package biz.nellemann.hmci

import spock.lang.Specification

class ManagedSystemTest  extends Specification {

    void "test processPcmJson for ManagedSystem"() {

        setup:
        def testFile = new File(getClass().getResource('/pcm-data-managed-system.json').toURI())
        def testJson = testFile.getText('UTF-8')

        when:
        ManagedSystem system = new ManagedSystem("e09834d1-c930-3883-bdad-405d8e26e166", "Test Name","Test Type", "Test Model", "Test S/N")
        system.processMetrics(testJson)

        then:
        system.metrics.systemUtil.utilSamples.first().serverUtil.memory.assignedMemToLpars.first() == 40960.000
        system.metrics.systemUtil.utilSamples.first().serverUtil.processor.totalProcUnits.first() == 24.000
        system.metrics.systemUtil.utilSamples.first().viosUtil.first().name == "VIOS1"
        system.metrics.systemUtil.utilSamples.first().viosUtil.first().memory.assignedMem.first() == 8192.000
        system.metrics.systemUtil.utilSamples.first().viosUtil.first().storage.genericPhysicalAdapters.first().transmittedBytes.first() == 9966.933
        system.metrics.systemUtil.utilSamples.first().viosUtil.first().storage.fiberChannelAdapters.first().numOfPorts == 3

    }

    void "test getMemoryMetrics"() {

        setup:
        def testFile = new File(getClass().getResource('/pcm-data-managed-system.json').toURI())
        def testJson = testFile.getText('UTF-8')
        ManagedSystem system = new ManagedSystem("e09834d1-c930-3883-bdad-405d8e26e166", "Test Name","Test Type", "Test Model", "Test S/N")

        when:
        system.processMetrics(testJson)
        List<Map> listOfMaps = system.getMemoryMetrics()

        then:
        listOfMaps.size() == 1
        listOfMaps.first().get("fields")['totalMem'] == 1048576.000
    }

    void "test getProcessorMetrics"() {

        setup:
        def testFile = new File(getClass().getResource('/pcm-data-managed-system.json').toURI())
        def testJson = testFile.getText('UTF-8')
        ManagedSystem system = new ManagedSystem("e09834d1-c930-3883-bdad-405d8e26e166", "Test Name","Test Type", "Test Model", "Test S/N")

        when:
        system.processMetrics(testJson)
        List<Map> listOfMaps = system.getProcessorMetrics()

        then:
        listOfMaps.size() == 1
        listOfMaps.first().get("fields")['availableProcUnits'] == 16.000
    }

    void "test getSystemSharedProcessorPools"() {

        setup:
        def testFile = new File(getClass().getResource('/pcm-data-managed-system.json').toURI())
        def testJson = testFile.getText('UTF-8')
        ManagedSystem system = new ManagedSystem("e09834d1-c930-3883-bdad-405d8e26e166", "Test Name","Test Type", "Test Model", "Test S/N")

        when:
        system.processMetrics(testJson)
        List<Map> listOfMaps = system.getSharedProcessorPools()

        then:
        listOfMaps.size() == 1
        listOfMaps.first().get("fields")['assignedProcUnits'] == 23.767
    }


    /*
    void "test getSharedAdapterMetrics"() {

        setup:
        def testFile = new File(getClass().getResource('/pcm-data-logical-partition.json').toURI())
        def testJson = testFile.getText('UTF-8')
        ManagedSystem system = new ManagedSystem("e09834d1-c930-3883-bdad-405d8e26e166", "Test Name","Test Type", "Test Model", "Test S/N")
        LogicalPartition lpar = new LogicalPartition("2DE05DB6-8AD5-448F-8327-0F488D287E82", "9Flash01", "OS400", system)

        when:
        lpar.processMetrics(testJson)
        List<Map> listOfMaps = lpar.getSharedAdapterMetrics()

        then:
        listOfMaps.size() == 1
        listOfMaps.first().get("fields")['receivedBytes'] == 276.467
        listOfMaps.first().get("tags")['sea'] == 'ent5'
    }*/

}
