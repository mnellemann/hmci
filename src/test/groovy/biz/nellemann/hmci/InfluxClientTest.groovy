package biz.nellemann.hmci

import spock.lang.Specification

class InfluxClientTest extends Specification {

    InfluxClient influxClient

    def setup() {
        influxClient = new InfluxClient("http://localhost:8086", "root", "", "hmci")
        influxClient.login()
    }

    def cleanup() {
        influxClient.logoff()
    }


    void "write ManagedSystem data to influx"() {

        setup:
        def testFile = new File(getClass().getResource('/pcm-data-managed-system.json').toURI())
        def testJson = testFile.getText('UTF-8')

        when:
        ManagedSystem system = new ManagedSystem("e09834d1-c930-3883-bdad-405d8e26e166", "TestSystem", "TestType", "TestModel", "Test s/n")
        system.processMetrics(testJson)
        influxClient.writeManagedSystem(system)

        then:
        system.metrics.systemUtil.utilInfo.name == "S822L-8247-213C1BA"

    }


    void "write LogicalPartition data to influx"() {

        setup:
        def testFile = new File(getClass().getResource('/pcm-data-logical-partition.json').toURI())
        def testJson = testFile.getText('UTF-8')

        when:
        ManagedSystem system = new ManagedSystem("e09834d1-c930-3883-bdad-405d8e26e166", "TestSystem", "TestType", "TestModel", "Test s/n")
        LogicalPartition lpar = new LogicalPartition("2DE05DB6-8AD5-448F-8327-0F488D287E82", "9Flash01", "OS400", system)

        lpar.processMetrics(testJson)
        influxClient.writeLogicalPartition(lpar)

        then:
        lpar.metrics.systemUtil.utilSamples.first().sampleInfo.status == 2

    }

}
