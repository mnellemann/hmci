package biz.nellemann.hmci

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import spock.lang.Specification

class HmcClientTest extends Specification {

    HmcClient hmc
    MockWebServer mockServer = new MockWebServer();


    def setup() {
        mockServer.start();
        hmc = new HmcClient(mockServer.url("/").toString(), "testUser", "testPassword")
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
        Map<String, ManagedSystem> systems = hmc.getManagedSystems()

        then:
        systems.size() == 2
        systems.get("e09834d1-c930-3883-bdad-405d8e26e166").name == "S822L-8247-213C1BA"
    }


    void "test getLogicalPartitionsForManagedSystem"() {
        setup:
        def testFile = new File(getClass().getResource('/logical-partitions.xml').toURI())
        def testXml = testFile.getText('UTF-8')
        mockServer.enqueue(new MockResponse().setBody(testXml));

        when:
        ManagedSystem system = new ManagedSystem("e09834d1-c930-3883-bdad-405d8e26e166", "Test Name","Test Type", "Test Model", "Test S/N")
        Map<String, LogicalPartition> partitions = hmc.getLogicalPartitionsForManagedSystem(system)

        then:
        partitions.size() == 12
        partitions.get("3380A831-9D22-4F03-A1DF-18B249F0FF8E").name == "AIX_Test1-e0f725f0-00000005"
        partitions.get("3380A831-9D22-4F03-A1DF-18B249F0FF8E").type == "AIX/Linux"
    }


    void "test getBody with JSON for ManagedSystem"() {
        setup:
        def testFile = new File(getClass().getResource('/pcm-data-managed-system.json').toURI())
        def testJson = testFile.getText('UTF-8')
        mockServer.enqueue(new MockResponse().setBody(testJson));

        when:
        String jsonString = hmc.getReponseBody(new URL(mockServer.url("/rest/api/pcm/ProcessedMetrics/ManagedSystem_e09834d1-c930-3883-bdad-405d8e26e166_20200807T122600+0200_20200807T122600+0200_30.json") as String))

        then:
        jsonString.contains('"uuid": "e09834d1-c930-3883-bdad-405d8e26e166"')
    }


    void "test getBody with JSON for LogicalPartition"() {
        setup:
        def testFile = new File(getClass().getResource('/pcm-data-logical-partition.json').toURI())
        def testJson = testFile.getText('UTF-8')
        mockServer.enqueue(new MockResponse().setBody(testJson));

        when:
        String jsonString = hmc.getReponseBody(new URL(mockServer.url("/rest/api/pcm/ProcessedMetrics/LogicalPartition_2DE05DB6-8AD5-448F-8327-0F488D287E82_20200807T123730+0200_20200807T123730+0200_30.json") as String))

        then:
        jsonString.contains('"uuid": "b597e4da-2aab-3f52-8616-341d62153559"')
    }


}
