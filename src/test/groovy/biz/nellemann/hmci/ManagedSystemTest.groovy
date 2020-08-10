package biz.nellemann.hmci

import spock.lang.Specification

class ManagedSystemTest  extends Specification {

    void "test processPcmJson for ManagedSystem"() {

        setup:
        def testFile = new File(getClass().getResource('/pcm-data-managed-system.json').toURI())
        def testJson = testFile.getText('UTF-8')

        when:
        ManagedSystem system = new ManagedSystem("e09834d1-c930-3883-bdad-405d8e26e166")
        system.processMetrics(testJson)

        then:
        system.metrics.systemUtil.utilSamples.first().serverUtil.memory.assignedMemToLpars.first() == 40960.000
        system.metrics.systemUtil.utilSamples.first().serverUtil.processor.totalProcUnits.first() == 24.000
        system.metrics.systemUtil.utilSamples.first().viosUtil.first().name == "VIOS1"
        system.metrics.systemUtil.utilSamples.first().viosUtil.first().memory.assignedMem.first() == 8192.000
        system.metrics.systemUtil.utilSamples.first().viosUtil.first().storage.genericPhysicalAdapters.first().transmittedBytes.first() == 9966.933
        system.metrics.systemUtil.utilSamples.first().viosUtil.first().storage.fiberChannelAdapters.first().numOfPorts == 3

    }

}
