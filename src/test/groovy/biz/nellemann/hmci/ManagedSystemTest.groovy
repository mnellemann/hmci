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
        listOfMeasurements.first().fields['mode'] == "share_idle_procs_active"
        listOfMeasurements.first().fields['entitledProcUnits'] == 1.0
        listOfMeasurements.first().fields['utilizedCappedProcUnits'] == 0.12
    }


    void "test getViosNetworkLpars"() {

        setup:
        def testFile = new File(getClass().getResource('/pcm-data-managed-system.json').toURI())
        def testJson = testFile.getText('UTF-8')
        ManagedSystem system = new ManagedSystem("e09834d1-c930-3883-bdad-405d8e26e166", "Test Name","Test Type", "Test Model", "Test S/N")

        when:
        system.processMetrics(testJson)
        List<Measurement> listOfMeasurements = system.getViosNetworkLpars()

        then:
        listOfMeasurements.size() == 2
        listOfMeasurements.first().tags['viosname'] == "VIOS1"
        listOfMeasurements.first().fields['clientlpars'] == 1
    }


    void "test getViosNetworkSharedAdapters"() {

        setup:
        def testFile = new File(getClass().getResource('/pcm-data-managed-system.json').toURI())
        def testJson = testFile.getText('UTF-8')
        ManagedSystem system = new ManagedSystem("e09834d1-c930-3883-bdad-405d8e26e166", "Test Name","Test Type", "Test Model", "Test S/N")

        when:
        system.processMetrics(testJson)
        List<Measurement> listOfMeasurements = system.getViosNetworkSharedAdapters()

        then:
        listOfMeasurements.size() == 2
        listOfMeasurements.first().tags['viosname'] == "VIOS1"
        listOfMeasurements.first().tags['location'] == "U8247.22L.213C1BA-V1-C2-T1"
        listOfMeasurements.first().fields['type'] == "sea"
        listOfMeasurements.first().fields['transferredBytes'] == 14180.2d
    }


    void "test getViosNetworkVirtualAdapters"() {

        setup:
        def testFile = new File(getClass().getResource('/pcm-data-managed-system.json').toURI())
        def testJson = testFile.getText('UTF-8')
        ManagedSystem system = new ManagedSystem("e09834d1-c930-3883-bdad-405d8e26e166", "Test Name","Test Type", "Test Model", "Test S/N")

        when:
        system.processMetrics(testJson)
        List<Measurement> listOfMeasurements = system.getViosNetworkVirtualAdapters()

        then:
        listOfMeasurements.size() == 4
        listOfMeasurements.first().tags['viosname'] == "VIOS1"
        listOfMeasurements.first().tags['location'] == "U8247.22L.213C1BA-V1-C2"
        listOfMeasurements.first().tags['vswitchid'] == "0"
        listOfMeasurements.first().fields['transferredBytes'] == 8245.4d
    }


    void "test getViosNetworkGenericAdapters"() {

        setup:
        def testFile = new File(getClass().getResource('/pcm-data-managed-system.json').toURI())
        def testJson = testFile.getText('UTF-8')
        ManagedSystem system = new ManagedSystem("e09834d1-c930-3883-bdad-405d8e26e166", "Test Name","Test Type", "Test Model", "Test S/N")

        when:
        system.processMetrics(testJson)
        List<Measurement> listOfMeasurements = system.getViosNetworkGenericAdapters()

        then:
        listOfMeasurements.size() == 6
        listOfMeasurements.first().tags['viosname'] == "VIOS1"
        listOfMeasurements.first().tags['location'] == "U78CB.001.WZS0BYF-P1-C10-T3"
        listOfMeasurements.first().fields['receivedBytes'] == 1614.567d
        listOfMeasurements.first().fields['sentBytes'] == 3511.833d
    }


    void "test getViosStorageLpars"() {

        setup:
        def testFile = new File(getClass().getResource('/pcm-data-managed-system.json').toURI())
        def testJson = testFile.getText('UTF-8')
        ManagedSystem system = new ManagedSystem("e09834d1-c930-3883-bdad-405d8e26e166", "Test Name","Test Type", "Test Model", "Test S/N")

        when:
        system.processMetrics(testJson)
        List<Measurement> listOfMeasurements = system.getViosStorageLpars()

        then:
        listOfMeasurements.size() == 2
        listOfMeasurements.first().tags['viosname'] == "VIOS1"
        listOfMeasurements.first().fields['clientlpars'] == 1
    }


    void "test getViosStorageFiberChannelAdapters"() {

        setup:
        def testFile = new File(getClass().getResource('/pcm-data-managed-system.json').toURI())
        def testJson = testFile.getText('UTF-8')
        ManagedSystem system = new ManagedSystem("e09834d1-c930-3883-bdad-405d8e26e166", "Test Name","Test Type", "Test Model", "Test S/N")

        when:
        system.processMetrics(testJson)
        List<Measurement> listOfMeasurements = system.getViosStorageFiberChannelAdapters()

        then:
        listOfMeasurements.size() == 4
        listOfMeasurements.first().tags['viosname'] == "VIOS1"
        listOfMeasurements.first().tags['location'] == "U78CB.001.WZS0BYF-P1-C12-T1"
        listOfMeasurements.first().fields['numOfReads'] == 0.0
        listOfMeasurements.first().fields['numOfWrites'] == 0.067d
    }


    void "test getViosStoragePhysicalAdapters"() {

        setup:
        def testFile = new File(getClass().getResource('/pcm-data-managed-system.json').toURI())
        def testJson = testFile.getText('UTF-8')
        ManagedSystem system = new ManagedSystem("e09834d1-c930-3883-bdad-405d8e26e166", "Test Name","Test Type", "Test Model", "Test S/N")

        when:
        system.processMetrics(testJson)
        List<Measurement> listOfMeasurements = system.getViosStoragePhysicalAdapters()

        then:
        listOfMeasurements.size() == 2
        listOfMeasurements.first().tags['viosname'] == "VIOS1"
        listOfMeasurements.first().tags['location'] == "U78CB.001.WZS0BYF-P1-C14-T1"
        listOfMeasurements.first().fields['numOfReads'] == 0.0
        listOfMeasurements.first().fields['numOfWrites'] == 19.467d
    }


    void "test getViosStorageVirtualAdapters"() {

        setup:
        def testFile = new File(getClass().getResource('/pcm-data-managed-system.json').toURI())
        def testJson = testFile.getText('UTF-8')
        ManagedSystem system = new ManagedSystem("e09834d1-c930-3883-bdad-405d8e26e166", "Test Name","Test Type", "Test Model", "Test S/N")

        when:
        system.processMetrics(testJson)
        List<Measurement> listOfMeasurements = system.getViosStorageVirtualAdapters()

        then:
        listOfMeasurements.size() == 3
        listOfMeasurements.first().tags['viosname'] == "VIOS1"
        listOfMeasurements.first().tags['location'] == "U8247.22L.213C1BA-V1-C6"
        listOfMeasurements.first().fields['numOfReads'] == 0.0
        listOfMeasurements.first().fields['numOfWrites'] == 0.0
    }

}
