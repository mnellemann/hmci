package biz.nellemann.hmci


import groovy.cli.picocli.CliBuilder
import groovy.cli.picocli.OptionAccessor
import groovy.util.logging.Slf4j

@Slf4j
class App implements Runnable {

    HmcClient hmc
    InfluxClient influx

    Map<String,ManagedSystem> systems = new HashMap<String, ManagedSystem>()
    Map<String, LogicalPartition> partitions = new HashMap<String, LogicalPartition>()


    App(String... args) {

    }


    void scanHmc() {

        try {

            if(hmc == null) {
                hmc = new HmcClient("https://10.32.64.39:12443", "hmci", "hmcihmci")
                hmc.login()
            }

            hmc.getManagedSystems().each { systemId, system ->

                // Add to list of known systems
                systems.putIfAbsent(systemId, system)

                // Get LPAR's for this system
                hmc.getLogicalPartitionsForManagedSystem(system).each { partitionId, partition ->

                    // Add to list of known partitions
                    partitions.putIfAbsent(partitionId, partition)
                }

            }

        } catch(Exception e) {
            log.error(e.message)
            hmc = null
        }

    }


    void getMetricsForSystems() {

        try {

            if(hmc == null) {
                hmc = new HmcClient("https://10.32.64.39:12443", "hmci", "hmcihmci")
                hmc.login()
            }

            systems.each {systemId, system ->

                // Get and process metrics for this system
                String tmpJsonString = hmc.getPcmDataForManagedSystem(system)
                if(tmpJsonString && !tmpJsonString.empty) {
                    system.processMetrics(tmpJsonString)
                }

            }

        } catch(Exception e) {
            log.error(e.message)
            hmc = null
        }

    }


    void getMetricsForPartitions() {

        try {

            if(hmc == null) {
                hmc = new HmcClient("https://10.32.64.39:12443", "hmci", "hmcihmci")
                hmc.login()
            }


            // Get LPAR's for this system
            partitions.each { partitionId, partition ->

                // Get and process metrics for this partition
                String tmpJsonString2 = hmc.getPcmDataForLogicalPartition(partition)
                if(tmpJsonString2 && !tmpJsonString2.empty) {
                    partition.processMetrics(tmpJsonString2)
                }

            }

        } catch(Exception e) {
            log.error(e.message)
            hmc = null
        }
    }

    void writeMetricsForManagedSystems() {

        if(!influx) {
            influx = new InfluxClient("http://127.0.0.1:8086", "root", "", "hmci")
            influx.login()
        }

        systems.each {systemId, system ->
            influx.writeManagedSystem(system)
        }
    }

    void writeMetricsForLogicalPartitions() {

        if(!influx) {
            influx = new InfluxClient("http://127.0.0.1:8086", "root", "", "hmci")
            influx.login()
        }

        partitions.each {partitionId, partition ->
            influx.writeLogicalPartition(partition)
        }
    }


    static void main(String... args) {

        def cli = new CliBuilder()
        cli.h(longOpt: 'help', 'display usage')
        cli.v(longOpt: 'version', 'display version')
        cli.c(longOpt: 'config', args: 1, required: true, defaultValue: '~/.config/hmci.toml', 'configuration file')

        OptionAccessor options = cli.parse(args)
        if (options.h) cli.usage()

        if(options.c) {
            File configurationFile = new File((String)options.config)
            if(configurationFile.exists()) {
                log.info("Configuration file found at: " + configurationFile.toString())
            } else {
                log.warn("No configuration file found at: " + configurationFile.toString())
            }
        }

        // TODO: Read configuration file or create new empty file,
        //       pass the properties or configuration bean to App.

        new App().run()
        System.exit(0);
    }

    
    @Override
    void run() {

        log.info("In RUN ")

        boolean keepRunning = true
        int numberOfRuns = 0

        // Do initial scan - TODO: should do this once in a while..
        scanHmc()

        while(keepRunning) {

            getMetricsForSystems()
            writeMetricsForManagedSystems()

            getMetricsForPartitions()
            writeMetricsForLogicalPartitions()

            // Refresh HMC
            if(numberOfRuns % 5) {
                scanHmc()
            }
            
            // Stop after some time
            if(numberOfRuns > 15) {
                keepRunning = false
            }

            numberOfRuns++
            Thread.sleep(60 * 1000)
        }

        hmc?.logoff()
        influx?.logoff()
    }

}