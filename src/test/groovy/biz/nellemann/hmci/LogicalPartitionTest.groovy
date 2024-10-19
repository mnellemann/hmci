package biz.nellemann.hmci

import biz.nellemann.hmci.dto.xml.LogicalPartitionEntry
import org.mockserver.integration.ClientAndServer
import org.mockserver.logging.MockServerLogger
import org.mockserver.socket.PortFactory
import org.mockserver.socket.tls.KeyStoreFactory
import spock.lang.Shared
import spock.lang.Specification


import javax.net.ssl.HttpsURLConnection

class LogicalPartitionTest extends Specification {

    @Shared
    private static ClientAndServer mockServer;

    @Shared
    private RestClient serviceClient

    @Shared
    private InfluxClient influxClient

    @Shared
    private ManagedSystem managedSystem

    @Shared
    private LogicalPartition logicalPartition

    @Shared
    private File metricsFile


    def setupSpec() {
        HttpsURLConnection.setDefaultSSLSocketFactory(new KeyStoreFactory(new MockServerLogger()).sslContext().getSocketFactory());
        mockServer = ClientAndServer.startClientAndServer(PortFactory.findFreePort());
        serviceClient = new RestClient(String.format("http://localhost:%d", mockServer.getPort()), "user", "password", false)
        MockResponses.prepareClientResponseForLogin(mockServer)
        MockResponses.prepareClientResponseForManagedSystem(mockServer)
        MockResponses.prepareClientResponseForVirtualIOServer(mockServer)
        MockResponses.prepareClientResponseForLogicalPartition(mockServer)
        serviceClient.login()

        managedSystem = new ManagedSystem(serviceClient, influxClient, String.format("%s/rest/api/uom/ManagementConsole/2c6b6620-e3e3-3294-aaf5-38e546ff672b/ManagedSystem/b597e4da-2aab-3f52-8616-341d62153559", serviceClient.baseUrl));
        managedSystem.discover()

        logicalPartition = managedSystem.logicalPartitions.first()
        logicalPartition.refresh()

        metricsFile = new File("src/test/resources/3-logical-partition-perf-data.json")
    }

    def cleanupSpec() {
        serviceClient.logoff()
        mockServer.stop()
    }

    def setup() {
    }


    def "check that we found 2 logical partitions"() {
        expect:
        managedSystem.logicalPartitions.size() == 18
    }


    def "check name of 1st virtual server"() {
        when:
        LogicalPartitionEntry entry = logicalPartition.entry

        then:
        entry.getName() == "rhel8-ocp-helper"
    }


    void "process metrics data"() {
        when:
        logicalPartition.deserialize(metricsFile.getText('UTF-8'))

        then:
        logicalPartition.metric != null
        logicalPartition.metric.samples.size() == 6;
    }


    void "test basic metrics"() {
        when:
        logicalPartition.deserialize(metricsFile.getText('UTF-8'))

        then:
        logicalPartition.metric.getSample().lparsUtil.memory.logicalMem == 16384.000
        logicalPartition.metric.getSample().lparsUtil.processor.utilizedProcUnits == 0.00793
        logicalPartition.metric.getSample().lparsUtil.network.virtualEthernetAdapters.first().receivedBytes == 54.0
    }


    void "test getDetails"() {

        when:
        logicalPartition.deserialize(metricsFile.getText('UTF-8'))
        List<MeasurementGroup> listOfMeasurements = logicalPartition.getInformation(0)

        then:
        listOfMeasurements.size() == 1
        listOfMeasurements.first().fields['affinityScore'] == 100.0
        listOfMeasurements.first().fields['osType'] == 'IBM i'
        listOfMeasurements.first().fields['type'] == 'IBMi'
    }


    void "test getMemoryMetrics"() {

        when:
        logicalPartition.deserialize(metricsFile.getText('UTF-8'))
        List<MeasurementGroup> listOfMeasurements = logicalPartition.getMemoryMetrics(0)

        then:
        listOfMeasurements.size() == 1
        listOfMeasurements.first().fields['logicalMem'] == 16384.0
        listOfMeasurements.first().tags['lparname'] == 'rhel8-ocp-helper'

    }


    void "test getProcessorMetrics"() {

        when:
        logicalPartition.deserialize(metricsFile.getText('UTF-8'))
        List<MeasurementGroup> listOfMeasurements = logicalPartition.getProcessorMetrics(0)

        then:
        listOfMeasurements.size() == 1
        listOfMeasurements.first().fields['utilizedProcUnits'] == 0.00793
        listOfMeasurements.first().tags['lparname'] == 'rhel8-ocp-helper'

    }


    void "test getVirtualEthernetAdapterMetrics"() {

        when:
        logicalPartition.deserialize(metricsFile.getText('UTF-8'))
        List<MeasurementGroup> listOfMeasurements = logicalPartition.getVirtualEthernetAdapterMetrics(0)

        then:
        listOfMeasurements.size() == 1
        listOfMeasurements.first().fields['receivedBytes'] == 54.0
        listOfMeasurements.first().tags['location'] == 'U9009.42A.21F64EV-V11-C7'
    }


    void "test getVirtualFiberChannelAdaptersMetrics"() {

        when:
        logicalPartition.deserialize(metricsFile.getText('UTF-8'))
        List<MeasurementGroup> listOfMeasurements = logicalPartition.getVirtualFibreChannelAdapterMetrics(0)

        then:
        listOfMeasurements.size() == 2
        listOfMeasurements.first().fields['writeBytes'] == 4454.4
        listOfMeasurements.first().tags['viosId'] == '1'

    }

}
