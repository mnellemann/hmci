package biz.nellemann.hmci

import org.mockserver.integration.ClientAndServer
import org.mockserver.logging.MockServerLogger
import org.mockserver.socket.PortFactory
import org.mockserver.socket.tls.KeyStoreFactory
import spock.lang.Shared
import spock.lang.Specification

import javax.net.ssl.HttpsURLConnection

class ManagedSystemTest extends Specification {

    @Shared
    private static ClientAndServer mockServer;

    @Shared
    private Session session = new Session();

    @Shared
    private RestClient serviceClient

    @Shared
    private ManagedSystem managedSystem

    @Shared
    private File metricsFile

    def setupSpec() {
        HttpsURLConnection.setDefaultSSLSocketFactory(new KeyStoreFactory(new MockServerLogger()).sslContext().getSocketFactory());
        mockServer = ClientAndServer.startClientAndServer(PortFactory.findFreePort());
        serviceClient = new RestClient(String.format("http://localhost:%d", mockServer.getPort()), "user", "password", false, 30)
        MockResponses.prepareClientResponseForLogin(mockServer)
        MockResponses.prepareClientResponseForManagementConsole(mockServer)
        MockResponses.prepareClientResponseForManagedSystem(mockServer)
        MockResponses.prepareClientResponseForVirtualIOServer(mockServer)
        MockResponses.prepareClientResponseForLogicalPartition(mockServer)
        serviceClient.login()
        session.setRestClient(serviceClient)
        managedSystem = new ManagedSystem(session, String.format("%s/rest/api/uom/ManagementConsole/2c6b6620-e3e3-3294-aaf5-38e546ff672b/ManagedSystem/b597e4da-2aab-3f52-8616-341d62153559", serviceClient.baseUrl));
        managedSystem.discover()
        metricsFile = new File(getClass().getResource('/2-managed-system-perf-data2.json').toURI())
    }

    def cleanupSpec() {
        serviceClient.logoff()
        mockServer.stop()
    }

    def setup() {
    }

    def "test we got entry"() {

        expect:
        managedSystem.entry.getName() == "Server-9009-42A-SN21F64EV"
    }

    void "test getInformation"() {

        when:
        managedSystem.deserialize(metricsFile.getText('UTF-8'))
        List<MeasurementBundle> bundles = managedSystem.doInformation(0)

        then:
        bundles.size() == 1
        bundles.first().tags['system'] == 'Server-9009-42A-SN21F64EV'
        bundles.first().items.first().getKey() == 'mtm'
        bundles.first().items.first().getStringValue() == '9009-42A 21F64EV'
    }

    void "test getMemoryMetrics"() {

        when:
        managedSystem.deserialize(metricsFile.getText('UTF-8'))
        List<MeasurementBundle> bundles = managedSystem.doMemoryMetrics(0)

        then:
        bundles.size() == 1
        bundles.first().tags['system'] == 'Server-9009-42A-SN21F64EV'
    }

    void "test getProcessorMetrics"() {

        when:
        managedSystem.deserialize(metricsFile.getText('UTF-8'))
        List<MeasurementBundle> bundles = managedSystem.doProcessorMetrics(0)

        then:
        bundles.size() == 1
        bundles.first().tags['system'] == 'Server-9009-42A-SN21F64EV'
        bundles.first().items.first().getKey() == 'installed_units'
        bundles.first().items.first().getDoubleValue() == 24.0d

    }

    /*
    void "test getSystemSharedProcessorPools"() {

        when:
        managedSystem.deserialize(metricsFile.getText('UTF-8'))
        List<MeasurementBundle> listOfMeasurements = managedSystem.getSharedProcessorPools(0)

        then:
        listOfMeasurements.size() == 4
        listOfMeasurements.first().fields['assigned_proc_units'] == 22.00013
    }*/

    /*
    void "test getPhysicalProcessorPool"() {
        when:
        managedSystem.deserialize(metricsFile.getText('UTF-8'))
        List<MeasurementBundle> listOfMeasurements = managedSystem.getPhysicalProcessorPool(0)

        then:
        listOfMeasurements.size() == 1
        listOfMeasurements.first().fields['assigned_proc_units'] == 22.0

    }*/


}

