package biz.nellemann.hmci

import spock.lang.Specification

import java.nio.file.Path
import java.nio.file.Paths


class ConfigurationTest extends Specification {

    Path testConfigurationFile = Paths.get(getClass().getResource('/hmci.toml').toURI())

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
