package biz.nellemann.hmci

import okhttp3.mockwebserver.MockWebServer
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


    void "write some managed system data to influx"() {

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


}
