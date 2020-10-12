package biz.nellemann.hmci

import spock.lang.Specification


class ConfigurationTest extends Specification {

    String testConfigurationFile = new File(getClass().getResource('/hmci.toml').toURI()).absolutePath

    void "test parsing"() {

        when:
        Configuration conf = new Configuration(testConfigurationFile)

        then:
        conf != null

    }

    void "test lookup influx"() {

        when:
        Configuration conf = new Configuration(testConfigurationFile)

        then:
        conf != null

    }

}
