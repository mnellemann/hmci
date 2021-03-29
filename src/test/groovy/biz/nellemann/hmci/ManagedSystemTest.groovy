package biz.nellemann.hmci

import spock.lang.Specification

class ManagedSystemTest extends Specification {

    void "test processPcmJson for ManagedSystem"() {

        setup:
        def testFile = new File(getClass().getResource('/pcm-data-managed-system.json').toURI())
        def testJson = testFile.getText('UTF-8')

        when:
        ManagedSystem system = new ManagedSystem("e09834d1-c930-3883-bdad-405d8e26e166", "Test Name","Test Type", "Test Model", "Test S/N")
        system.processMetrics(testJson)

        then:
        system.metrics.systemUtil.sample.serverUtil.memory.assignedMemToLpars == 40960.000
        system.metrics.systemUtil.sample.serverUtil.processor.totalProcUnits == 24.000
        system.metrics.systemUtil.sample.viosUtil.first().name == "VIOS1"
        system.metrics.systemUtil.sample.viosUtil.first().memory.assignedMem == 8192.0
        system.metrics.systemUtil.sample.viosUtil.first().storage.genericPhysicalAdapters.first().transmittedBytes == 9966.933
        system.metrics.systemUtil.sample.viosUtil.first().storage.fiberChannelAdapters.first().numOfPorts == 3

    }

    void "test getDetails"() {

        setup:
        def testFile = new File(getClass().getResource('/pcm-data-managed-system.json').toURI())
        def testJson = testFile.getText('UTF-8')
        ManagedSystem system = new ManagedSystem("e09834d1-c930-3883-bdad-405d8e26e166", "Test Name","Test Type", "Test Model", "Test S/N")

        when:
        system.processMetrics(testJson)
        List<Measurement> listOfMeasurements = system.getDetails()

        then:
        listOfMeasurements.size() == 1
        listOfMeasurements.first().tags['servername'] == 'Test Name'
        listOfMeasurements.first().fields['utilizedProcUnits'] == 0.0
        listOfMeasurements.first().fields['assignedMem'] == 5632.0
    }

    void "test getMemoryMetrics"() {

        setup:
        def testFile = new File(getClass().getResource('/pcm-data-managed-system.json').toURI())
        def testJson = testFile.getText('UTF-8')
        ManagedSystem system = new ManagedSystem("e09834d1-c930-3883-bdad-405d8e26e166", "Test Name","Test Type", "Test Model", "Test S/N")

        when:
        system.processMetrics(testJson)
        List<Measurement> listOfMeasurements = system.getMemoryMetrics()

        then:
        listOfMeasurements.size() == 1
        listOfMeasurements.first().fields['totalMem'] == 1048576.000
    }

    void "test getProcessorMetrics"() {

        setup:
        def testFile = new File(getClass().getResource('/pcm-data-managed-system.json').toURI())
        def testJson = testFile.getText('UTF-8')
        ManagedSystem system = new ManagedSystem("e09834d1-c930-3883-bdad-405d8e26e166", "Test Name","Test Type", "Test Model", "Test S/N")

        when:
        system.processMetrics(testJson)
        List<Measurement> listOfMeasurements = system.getProcessorMetrics()

        then:
        listOfMeasurements.size() == 1
        listOfMeasurements.first().fields['availableProcUnits'] == 16.000
    }

    void "test getSystemSharedProcessorPools"() {

        setup:
        def testFile = new File(getClass().getResource('/pcm-data-managed-system.json').toURI())
        def testJson = testFile.getText('UTF-8')
        ManagedSystem system = new ManagedSystem("e09834d1-c930-3883-bdad-405d8e26e166", "Test Name","Test Type", "Test Model", "Test S/N")

        when:
        system.processMetrics(testJson)
        List<Measurement> listOfMeasurements = system.getSharedProcessorPools()

        then:
        listOfMeasurements.size() == 1
        listOfMeasurements.first().fields['assignedProcUnits'] == 23.767
    }

    void "test VIOS data"() {
        setup:
        def testFile = new File(getClass().getResource('/pcm-data-managed-system.json').toURI())
        def testJson = testFile.getText('UTF-8')
        ManagedSystem system = new ManagedSystem("e09834d1-c930-3883-bdad-405d8e26e166", "Test Name","Test Type", "Test Model", "Test S/N")

        when:
        system.processMetrics(testJson)
        List<Measurement> listOfMeasurements = system.getSharedProcessorPools()

        then:
        listOfMeasurements.size() == 1
        listOfMeasurements.first().fields['assignedProcUnits'] == 23.767
    }

    void "test getViosMemoryMetrics"() {

        setup:
        def testFile = new File(getClass().getResource('/pcm-data-managed-system.json').toURI())
        def testJson = testFile.getText('UTF-8')
        ManagedSystem system = new ManagedSystem("e09834d1-c930-3883-bdad-405d8e26e166", "Test Name","Test Type", "Test Model", "Test S/N")

        when:
        system.processMetrics(testJson)
        List<Measurement> listOfMeasurements = system.getViosMemoryMetrics()

        then:
        listOfMeasurements.size() == 2
        listOfMeasurements.first().fields['assignedMem'] == 8192.000
        listOfMeasurements.first().fields['utilizedMem'] == 2093.000
    }

    void "test getViosProcessorMetrics"() {

        setup:
        def testFile = new File(getClass().getResource('/pcm-data-managed-system.json').toURI())
        def testJson = testFile.getText('UTF-8')
        ManagedSystem system = new ManagedSystem("e09834d1-c930-3883-bdad-405d8e26e166", "Test Name","Test Type", "Test Model", "Test S/N")

        when:
        system.processMetrics(testJson)
        List<Measurement> listOfMeasurements = system.getViosProcessorMetrics()

        then:
        listOfMeasurements.size() == 2
        listOfMeasurements.first().fields['entitledProcUnits'] == 1.0
        listOfMeasurements.first().fields['utilizedCappedProcUnits'] == 0.12
    }

}
