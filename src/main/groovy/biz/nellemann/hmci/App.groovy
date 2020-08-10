package biz.nellemann.hmci


import groovy.cli.picocli.CliBuilder
import groovy.cli.picocli.OptionAccessor
import groovy.util.logging.Slf4j

@Slf4j
class App {

    HmcClient hmc

    Map<String,ManagedSystem> systems = new HashMap<String, ManagedSystem>()
    Map<String, LogicalPartition> partitions = new HashMap<String, LogicalPartition>()


    App(String... args) {

        def cli = new CliBuilder()
        cli.h(longOpt: 'help', 'display usage')
        cli.v(longOpt: 'version', 'display version')
        cli.c(longOpt: 'config', args: 1, required: true, defaultValue: '~/.config/hmci.properties', 'configuration file')


        OptionAccessor options = cli.parse(args)
        if (options.h) cli.usage()

        if(options.c) {
            //println("TODO: Use configuration file: " + options.config)
        }

        // TODO: Read configuration file or create new empty file,
        //       pass the properties or configuration bean to App.

        println("HMC Insights")

        hmc = new HmcClient("https://10.32.64.39:12443", "hmci", "hmcihmci")
        hmc.login()

        scan()

        metricsForSystems()

        metricsForPartitions()

        hmc?.logoff()

    }


    void scan() {

        try {

            if(hmc == null) {
                hmc = new HmcClient("https://10.32.64.39:12443", "hmci", "hmcihmci")
                hmc.login()
            }

            hmc.getManagedSystems().each { systemId, system ->

                // Add to list of known systems
                systems.putIfAbsent(systemId, system)

                // Get LPAR's for this system
                hmc.getLogicalPartitionsForManagedSystemWithId(systemId).each { partitionId, partition ->

                    // Add to list of known partitions
                    partitions.putIfAbsent(partitionId, partition)
                }

            }

        } catch(Exception e) {
            log.error(e.message)
            hmc = null
        }

    }


    void metricsForSystems() {

        try {

            if(hmc == null) {
                hmc = new HmcClient("https://10.32.64.39:12443", "hmci", "hmcihmci")
                hmc.login()
            }

            systems.each {systemId, system ->

                // Get and process metrics for this system
                String tmpJsonString = hmc.getPcmDataForManagedSystem(systemId)
                if(tmpJsonString && !tmpJsonString.empty) {
                    system.processMetrics(tmpJsonString)
                }

            }

        } catch(Exception e) {
            log.error(e.message)
            hmc = null
        }

    }

    void metricsForPartitions() {

        try {

            if(hmc == null) {
                hmc = new HmcClient("https://10.32.64.39:12443", "hmci", "hmcihmci")
                hmc.login()
            }


            // Get LPAR's for this system
            partitions.each { partitionId, partition ->

                // Get and process metrics for this partition
                String tmpJsonString2 = hmc.getPcmDataForLogicalPartition(partition.systemId, partitionId)
                if(tmpJsonString2 && !tmpJsonString2.empty) {
                    partition.processMetrics(tmpJsonString2)
                }

            }

        } catch(Exception e) {
            log.error(e.message)
            hmc = null
        }
    }


    static void main(String... args) {
        new App(args)
        System.exit(0);
    }

}