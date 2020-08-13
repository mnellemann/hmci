package biz.nellemann.hmci


import groovy.cli.picocli.CliBuilder
import groovy.cli.picocli.OptionAccessor
import groovy.util.logging.Slf4j

@Slf4j
class App implements Runnable {

    HmcClient hmc
    InfluxClient influx

    final ConfigObject configuration
    final Integer refreshEverySec
    final Integer rescanHmcEvery

    Map<String, HmcClient> discoveredHmc = new HashMap<>()
    Map<String,ManagedSystem> systems = new HashMap<String, ManagedSystem>()
    Map<String, LogicalPartition> partitions = new HashMap<String, LogicalPartition>()


    App(ConfigObject configuration) {
        log.debug configuration.toString()
        this.configuration = configuration

        refreshEverySec = (Integer)configuration.get('hmci.refresh') ?: 60
        rescanHmcEvery = (Integer)configuration.get('hmci.rescan') ?: 15

        try {
            influx = new InfluxClient((String) configuration.get('influx')['url'], (String) configuration.get('influx')['username'], (String) configuration.get('influx')['password'], (String) configuration.get('influx')['database'])
            influx.login()
        } catch(Exception e) {
            System.exit(1)
        }

        // Initial scan
        discover()

        run()
    }


    void discover() {

        configuration.get('hmc').each { Object key, Object hmc ->
            if(!discoveredHmc?.containsKey(key)) {
                log.info("Adding HMC: " + hmc.toString())
                HmcClient hmcClient = new HmcClient(key as String, hmc['url'] as String, hmc['username'] as String, hmc['password'] as String, hmc['unsafe'] as Boolean)
                discoveredHmc.put(key as String, hmcClient)
            }
        }

        discoveredHmc.each {id, hmcClient ->

            try {
                hmcClient.login()
                hmcClient.getManagedSystems().each { systemId, system ->

                    // Add to list of known systems
                    systems.putIfAbsent(systemId, system)

                    // Get LPAR's for this system
                    hmcClient.getLogicalPartitionsForManagedSystem(system).each { partitionId, partition ->

                        // Add to list of known partitions
                        partitions.putIfAbsent(partitionId, partition)
                    }
                }
            } catch(Exception e) {
                log.error("discover() - " + id + " error: " + e.message)
                discoveredHmc.remove(id)
            }

        }

    }


    void getMetricsForSystems() {

        try {

            systems.each {systemId, system ->

                HmcClient hmcClient = discoveredHmc.get(system.hmcId)

                // Get and process metrics for this system
                String tmpJsonString = hmcClient.getPcmDataForManagedSystem(system)
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

            // Get LPAR's for this system
            partitions.each { partitionId, partition ->

                HmcClient hmcClient = discoveredHmc.get(partition.system.hmcId)

                // Get and process metrics for this partition
                String tmpJsonString2 = hmcClient.getPcmDataForLogicalPartition(partition)
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
        systems.each {systemId, system ->
            influx.writeManagedSystem(system)
        }
    }


    void writeMetricsForLogicalPartitions() {
        partitions.each {partitionId, partition ->
            influx.writeLogicalPartition(partition)
        }
    }


    static void main(String... args) {

        def cli = new CliBuilder()
        cli.h(longOpt: 'help', 'display usage')
        cli.v(longOpt: 'version', 'display version')
        cli.c(longOpt: 'config', args: 1, required: true, defaultValue: '/opt/hmci/conf/hmci.groovy', 'configuration file')

        OptionAccessor options = cli.parse(args)
        if (options.h) cli.usage()

        ConfigObject configuration
        if(options.c) {

            File configurationFile = new File((String)options.config)
            if(!configurationFile.exists()) {
                println("No configuration file found at: " + configurationFile.toString())
                System.exit(1)
            }

            // Read in 'config.groovy' for the development environment.
            configuration = new ConfigSlurper("development").parse(configurationFile.toURI().toURL());

            // Flatten configuration for easy access keys with dotted notation.
            //configuration = conf.flatten();
        }

        new App(configuration)
        System.exit(0);
    }


    @Override
    void run() {

        log.info("In RUN ")

        boolean keepRunning = true
        int executions = 0

        while(keepRunning) {

            getMetricsForSystems()
            writeMetricsForManagedSystems()

            getMetricsForPartitions()
            writeMetricsForLogicalPartitions()

            // Refresh HMC's
            if(executions % rescanHmcEvery) {
                discover()
            }

            executions++
            Thread.sleep(refreshEverySec * 1000)
        }

    }

}
