package biz.nellemann.hmci

import biz.nellemann.hmci.pojo.ManagedSystem
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
        hmc.managedSystems[0].id == "e09834d1-c930-3883-bdad-405d8e26e166"
        hmc.managedSystems[0].name == "S822L-8247-213C1BA"
    }

    void "test getLogicalPartitionsForManagedSystem"() {
        setup:
        def testFile = new File(getClass().getResource('/logical-partitions.xml').toURI())
        def testXml = testFile.getText('UTF-8')
        mockServer.enqueue(new MockResponse().setBody(testXml));

        when:
        ManagedSystem system = new ManagedSystem("e09834d1-c930-3883-bdad-405d8e26e166")
        hmc.managedSystems.add(system)
        hmc.getLogicalPartitionsForManagedSystem(system)

        then:
        hmc.managedSystems[0].partitions.size() == 12
        hmc.managedSystems[0].partitions[0].id == "3380A831-9D22-4F03-A1DF-18B249F0FF8E"
        hmc.managedSystems[0].partitions[0].name == "AIX_Test1-e0f725f0-00000005"
        hmc.managedSystems[0].partitions[0].type == "AIX/Linux"
    }

    /*
    void "test getSystemPCMLinks"() {
        setup:
        def testFile = new File(getClass().getResource('/managed-system-pcm.xml').toURI())
        def testXml = testFile.getText('UTF-8')
        mockServer.enqueue(new MockResponse().setBody(testXml));
        ManagedSystem managedSystem = new ManagedSystem("e09834d1-c930-3883-bdad-405d8e26e166")

        when:
        List<String> links = hmc.getManagedSystemProcessedMetrics(managedSystem)

        then:
        links.size() == 1
        //links[0] == "https://10.32.64.39:12443/rest/api/pcm/ProcessedMetrics/ManagedSystem_b597e4da-2aab-3f52-8616-341d62153559_20200806T183800+0200_20200806T184000+0200_30.json"


    }

    void "test getPartitionPCMLinks"() {

        setup:
        def testFile = new File(getClass().getResource('/managed-system-pcm.xml').toURI())
        def testXml = testFile.getText('UTF-8')
        mockServer.enqueue(new MockResponse().setBody(testXml));
        ManagedSystem system = new ManagedSystem()

        when:
        List<String> links = hmc.getPartitionPCMLinks("e09834d1-c930-3883-bdad-405d8e26e166")

        then:
        links.size() == 12
        links[0] == "https://10.32.64.39:12443/rest/api/pcm/ManagedSystem/b597e4da-2aab-3f52-8616-341d62153559/LogicalPartition/44A89632-E9E6-4E12-91AF-1A33DEE060CF/ProcessedMetrics?NoOfSamples=5"

    }
     */

}
