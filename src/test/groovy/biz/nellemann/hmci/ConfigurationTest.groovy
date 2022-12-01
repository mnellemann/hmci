package biz.nellemann.hmci

import biz.nellemann.hmci.dto.toml.Configuration
import biz.nellemann.hmci.dto.toml.HmcConfiguration
import com.fasterxml.jackson.dataformat.toml.TomlMapper
import spock.lang.Specification

import java.nio.file.Path
import java.nio.file.Paths


class ConfigurationTest extends Specification {

    Path testConfigurationFile = Paths.get(getClass().getResource('/hmci.toml').toURI())

    TomlMapper mapper

    def setup() {
        mapper = new TomlMapper();
    }

    def cleanup() {
    }


    void "test parsing of configuration file"() {

        when:
        Configuration conf = mapper.readerFor(Configuration.class).readValue(testConfigurationFile.toFile())

        println(conf.hmc.entrySet().forEach((e) -> {
            println((String)e.key + " -> " + e);

            HmcConfiguration c = e.value;
            println(c.url);
        }));

        then:
        conf != null
    }

    void "test HMC energy flag, default setting"() {

        when:
        Configuration conf = mapper.readerFor(Configuration.class).readValue(testConfigurationFile.toFile())

        then:
        !conf.hmc.get("site1").energy

    }

    void "test HMC exclude and include options"() {

        when:
        Configuration conf = mapper.readerFor(Configuration.class).readValue(testConfigurationFile.toFile())

        then:
        conf.hmc.get("site1").excludeSystems.contains("notThisSys")
        conf.hmc.get("site1").includeSystems.contains("onlyThisSys")
        conf.hmc.get("site1").excludePartitions.contains("notThisPartition")
        conf.hmc.get("site1").includePartitions.contains("onlyThisPartition")

    }

}
