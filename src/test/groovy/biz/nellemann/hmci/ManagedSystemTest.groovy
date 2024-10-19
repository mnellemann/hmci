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
    private RestClient serviceClient

    @Shared
    private InfluxClient influxClient

    @Shared
    private ManagedSystem managedSystem

    @Shared
    private File metricsFile

    def setupSpec() {
        HttpsURLConnection.setDefaultSSLSocketFactory(new KeyStoreFactory(new MockServerLogger()).sslContext().getSocketFactory());
        mockServer = ClientAndServer.startClientAndServer(PortFactory.findFreePort());
        serviceClient = new RestClient(String.format("http://localhost:%d", mockServer.getPort()), "user", "password", false)
        MockResponses.prepareClientResponseForLogin(mockServer)
        MockResponses.prepareClientResponseForManagementConsole(mockServer)
        MockResponses.prepareClientResponseForManagedSystem(mockServer)
        MockResponses.prepareClientResponseForVirtualIOServer(mockServer)
        MockResponses.prepareClientResponseForLogicalPartition(mockServer)
        serviceClient.login()
        managedSystem = new ManagedSystem(serviceClient, influxClient, String.format("%s/rest/api/uom/ManagementConsole/2c6b6620-e3e3-3294-aaf5-38e546ff672b/ManagedSystem/b597e4da-2aab-3f52-8616-341d62153559", serviceClient.baseUrl));
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

    void "test getDetails"() {

        when:
        managedSystem.deserialize(metricsFile.getText('UTF-8'))
        List<MeasurementGroup> listOfMeasurements = managedSystem.getInformation(0)

        then:
        listOfMeasurements.size() == 1
        listOfMeasurements.first().tags['servername'] == 'Server-9009-42A-SN21F64EV'
        listOfMeasurements.first().fields['utilizedProcUnits'] == 0.00458
        listOfMeasurements.first().fields['assignedMem'] == 40448.0
    }

    void "test getMemoryMetrics"() {

        when:
        managedSystem.deserialize(metricsFile.getText('UTF-8'))
        List<MeasurementGroup> listOfMeasurements = managedSystem.getMemoryMetrics(0)

        then:
        listOfMeasurements.size() == 1
        listOfMeasurements.first().fields['totalMem'] == 1048576.000
    }

    void "test getProcessorMetrics"() {

        when:
        managedSystem.deserialize(metricsFile.getText('UTF-8'))
        List<MeasurementGroup> listOfMeasurements = managedSystem.getProcessorMetrics(0)

        then:
        listOfMeasurements.size() == 1
        listOfMeasurements.first().fields['availableProcUnits'] == 4.65
    }

    void "test getSystemSharedProcessorPools"() {

        when:
        managedSystem.deserialize(metricsFile.getText('UTF-8'))
        List<MeasurementGroup> listOfMeasurements = managedSystem.getSharedProcessorPools(0)

        then:
        listOfMeasurements.size() == 4
        listOfMeasurements.first().fields['assignedProcUnits'] == 22.00013
    }

    void "test getPhysicalProcessorPool"() {
        when:
        managedSystem.deserialize(metricsFile.getText('UTF-8'))
        List<MeasurementGroup> listOfMeasurements = managedSystem.getPhysicalProcessorPool(0)

        then:
        listOfMeasurements.size() == 1
        listOfMeasurements.first().fields['assignedProcUnits'] == 22.0

    }


}

