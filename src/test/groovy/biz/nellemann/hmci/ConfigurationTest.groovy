package biz.nellemann.hmci

import spock.lang.Specification

import java.nio.file.Path
import java.nio.file.Paths


class ConfigurationTest extends Specification {

    Path testConfigurationFile = Paths.get(getClass().getResource('/hmci.toml').toURI())

    void "test parsing of configuration file"() {

        when:
        Configuration conf = new Configuration(testConfigurationFile)

        then:
        conf != null

    }

    void "test energy flag, default setting"() {

        when:
        Configuration conf = new Configuration(testConfigurationFile)

        then:
        !conf.getHmc().get(0).energy

    }

    void "test exclude and include options"() {

        when:
        Configuration conf = new Configuration(testConfigurationFile)

        then:
        conf.getHmc().get(0).excludeSystems.contains("notThisSys")
        conf.getHmc().get(0).includeSystems.contains("onlyThisSys")
        conf.getHmc().get(0).excludePartitions.contains("notThisPartition")
        conf.getHmc().get(0).includePartitions.contains("onlyThisPartition")

    }

}
