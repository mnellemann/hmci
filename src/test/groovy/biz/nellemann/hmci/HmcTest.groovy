package biz.nellemann.hmci


import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import spock.lang.Specification

class HmcTest extends Specification {

    Hmc hmc
    MockWebServer mockServer = new MockWebServer();


    def setup() {
        mockServer.start();
        hmc = new Hmc(mockServer.url("/").toString(), "testUser", "testPassword")
        hmc.authToken = "blaBla"
    }


    def cleanup() {
        mockServer.shutdown()
    }


    void "test getManagedSystems"() {
        setup:
        def testFile = new File(getClass().getResource('/managed-systems.xml').toURI())
        def testXml = testFile.getText('UTF-8')
        mockServer.enqueue(new MockResponse().setBody(testXml));

        when:
        hmc.getManagedSystems()

        then:
        hmc.managedSystems.size() == 2
        hmc.managedSystems.get("e09834d1-c930-3883-bdad-405d8e26e166").name == "S822L-8247-213C1BA"
    }


    void "test getLogicalPartitionsForManagedSystem"() {
        setup:
        def testFile = new File(getClass().getResource('/logical-partitions.xml').toURI())
        def testXml = testFile.getText('UTF-8')
        mockServer.enqueue(new MockResponse().setBody(testXml));

        when:
        ManagedSystem system = new ManagedSystem("e09834d1-c930-3883-bdad-405d8e26e166")
        hmc.managedSystems.put("e09834d1-c930-3883-bdad-405d8e26e166", system)
        hmc.getLogicalPartitionsForManagedSystem(system)

        then:
        system.partitions.size() == 12
        system.partitions.get("3380A831-9D22-4F03-A1DF-18B249F0FF8E").name == "AIX_Test1-e0f725f0-00000005"
        system.partitions.get("3380A831-9D22-4F03-A1DF-18B249F0FF8E").type == "AIX/Linux"
    }


    void "test getPcmJsonForManagedSystem"() {
        setup:
        def testFile = new File(getClass().getResource('/managed-system-pcm.json').toURI())
        def testJson = testFile.getText('UTF-8')
        mockServer.enqueue(new MockResponse().setBody(testJson));

        when:
        String jsonString = hmc.getPcmJsonForManagedSystem(mockServer.url("/rest/api/pcm/ProcessedMetrics/ManagedSystem_e09834d1-c930-3883-bdad-405d8e26e166_20200807T122600+0200_20200807T122600+0200_30.json").toString())

        then:
        jsonString.contains('"uuid": "e09834d1-c930-3883-bdad-405d8e26e166"')
    }


    void "test getPcmJsonForLogicalPartition"() {
        setup:
        def testFile = new File(getClass().getResource('/logical-partition-pcm.json').toURI())
        def testJson = testFile.getText('UTF-8')
        mockServer.enqueue(new MockResponse().setBody(testJson));

        when:
        String jsonString = hmc.getPcmJsonForLogicalPartition(mockServer.url("/rest/api/pcm/ProcessedMetrics/LogicalPartition_2DE05DB6-8AD5-448F-8327-0F488D287E82_20200807T123730+0200_20200807T123730+0200_30.json").toString())

        then:
        jsonString.contains('"uuid": "b597e4da-2aab-3f52-8616-341d62153559"')
    }


    void "test processPcmJsonForManagedSystem"() {

        setup:
        def testFile = new File(getClass().getResource('/managed-system-pcm.json').toURI())
        def testJson = testFile.getText('UTF-8')

        when:
        ManagedSystem system = new ManagedSystem("e09834d1-c930-3883-bdad-405d8e26e166")
        hmc.managedSystems.put("e09834d1-c930-3883-bdad-405d8e26e166", system)
        hmc.processPcmJsonForManagedSystem(testJson)

        then:
        system.metrics.systemUtil.utilSamples.first().serverUtil.memory.assignedMemToLpars.first() == 40960.000
        system.metrics.systemUtil.utilSamples.first().serverUtil.processor.totalProcUnits.first() == 24.000
        system.metrics.systemUtil.utilSamples.first().viosUtil.first().name == "VIOS1"
        system.metrics.systemUtil.utilSamples.first().viosUtil.first().memory.assignedMem.first() == 8192.000
        system.metrics.systemUtil.utilSamples.first().viosUtil.first().storage.genericPhysicalAdapters.first().transmittedBytes.first() == 2321.067
        system.metrics.systemUtil.utilSamples.first().viosUtil.first().storage.fiberChannelAdapters.first().numOfPorts == 3

    }

    void "test processPcmJsonForLogicalPartition"() {

        setup:
        def testFile = new File(getClass().getResource('/logical-partition-pcm.json').toURI())
        def testJson = testFile.getText('UTF-8')

        when:
        ManagedSystem system = new ManagedSystem("b597e4da-2aab-3f52-8616-341d62153559")
        hmc.managedSystems.put("b597e4da-2aab-3f52-8616-341d62153559", system)
        LogicalPartition lpar = new LogicalPartition("2DE05DB6-8AD5-448F-8327-0F488D287E82")
        system.partitions.put("2DE05DB6-8AD5-448F-8327-0F488D287E82", lpar)
        hmc.processPcmJsonForLogicalPartition(testJson)

        then:
        lpar.metrics.utilSamples.first().lparsUtil.first().memory.logicalMem.first() == 112640.000
        lpar.metrics.utilSamples.first().lparsUtil.first().processor.utilizedProcUnits.first() == 0.574
        lpar.metrics.utilSamples.first().lparsUtil.first().network.virtualEthernetAdapters.first().receivedPackets.first() == 11.933
    }


}
