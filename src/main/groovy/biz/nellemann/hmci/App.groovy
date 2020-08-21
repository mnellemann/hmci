/**
 *    Copyright 2020 Mark Nellemann <mark.nellemann@gmail.com>
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package biz.nellemann.hmci

import groovy.cli.picocli.CliBuilder
import groovy.cli.picocli.OptionAccessor
import groovy.util.logging.Slf4j

@Slf4j
class App implements Runnable {

    final ConfigObject configuration
    final Integer refreshEverySec
    final Integer rescanHmcEvery

    InfluxClient influxClient
    Map<String, HmcClient> hmcClients = new HashMap<>()
    Map<String,ManagedSystem> systems = new HashMap<String, ManagedSystem>()
    Map<String, LogicalPartition> partitions = new HashMap<String, LogicalPartition>()


    App(ConfigObject configuration) {
        this.configuration = configuration
        log.debug configuration.toString()

        refreshEverySec = (Integer)configuration.get('hmci.refresh') ?: 60
        rescanHmcEvery = (Integer)configuration.get('hmci.rescan') ?: 15

        String influxUrl = configuration.get('influx')['url']
        String influxUsername = configuration.get('influx')['username']
        String influxPassword = configuration.get('influx')['password']
        String influxDatabase = configuration.get('influx')['database']

        try {
            influxClient = new InfluxClient(influxUrl, influxUsername, influxPassword, influxDatabase)
            influxClient.login()
        } catch(Exception e) {
            System.exit(1)
        }

        // Initial scan
        discover()

        run()
    }


    void discover() {

        configuration.get('hmc').each { Object key, Object hmc ->
            if(!hmcClients?.containsKey(key)) {
                log.info("Adding HMC: " + hmc.toString())
                String hmcKey = key
                String hmcUrl = hmc['url']
                String hmcUsername = hmc['username']
                String hmcPassword = hmc['password']
                Boolean hmcUnsafe = hmc['unsafe']
                HmcClient hmcClient = new HmcClient(hmcKey, hmcUrl, hmcUsername, hmcPassword, hmcUnsafe)
                hmcClients.put(hmcKey, hmcClient)
            }
        }

        hmcClients.each { hmcId, hmcClient ->


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
                log.error("discover() - " + hmcId + " error: " + e.message)
                //hmcClients.remove(hmcId)
            }

        }

    }


    void getMetricsForSystems() {

        try {

            systems.each {systemId, system ->

                HmcClient hmcClient = hmcClients.get(system.hmcId)

                // Get and process metrics for this system
                String tmpJsonString = hmcClient.getPcmDataForManagedSystem(system)
                if(tmpJsonString && !tmpJsonString.empty) {
                    system.processMetrics(tmpJsonString)
                }

            }

        } catch(Exception e) {
            log.error(e.message)
        }

    }


    void getMetricsForPartitions() {

        try {

            // Get LPAR's for this system
            partitions.each { partitionId, partition ->

                HmcClient hmcClient = hmcClients.get(partition.system.hmcId)

                // Get and process metrics for this partition
                String tmpJsonString2 = hmcClient.getPcmDataForLogicalPartition(partition)
                if(tmpJsonString2 && !tmpJsonString2.empty) {
                    partition.processMetrics(tmpJsonString2)
                }

            }

        } catch(Exception e) {
            log.error(e.message)
        }
    }


    void writeMetricsForManagedSystems() {
        systems.each {systemId, system ->
            influxClient.writeManagedSystem(system)
        }
    }


    void writeMetricsForLogicalPartitions() {
        partitions.each {partitionId, partition ->
            influxClient.writeLogicalPartition(partition)
        }
    }


    static void main(String... args) {

        def cli = new CliBuilder(name: "hmci")
        cli.h(longOpt: 'help', 'display usage')
        cli.v(longOpt: 'version', 'display version')
        cli.c(longOpt: 'config', args: 1, required: true, defaultValue: '/etc/hmci.groovy', 'configuration file')

        OptionAccessor options = cli.parse(args)
        if (options.h) cli.usage()

        if(options.v) {
            // TODO - how to display correct version or build number ?
            println("See https://bitbucket.org/mnellemann/hmci for more information.")
            System.exit(0)
        }

        ConfigObject configuration
        if(options.c) {

            File configurationFile = new File((String)options.config)
            if(!configurationFile.exists()) {
                println("Error - No configuration file found at: " + configurationFile.toString())
                System.exit(1)
            }

            configuration = new ConfigSlurper("development").parse(configurationFile.toURI().toURL());
        }

        if(configuration == null || configuration.isEmpty()) {
            println("Error - Empty or faulty configuration")
            System.exit(1)
        }

        new App(configuration)
        System.exit(0);
    }


    @Override
    void run() {

        log.debug("run()")

        boolean keepRunning = true
        int executions = 0

        while(keepRunning) {

            try {
                getMetricsForSystems()
                getMetricsForPartitions()

                writeMetricsForManagedSystems()
                writeMetricsForLogicalPartitions()
                influxClient.writeBatchPoints()

                // Refresh HMC's
                if(executions > rescanHmcEvery) {
                    executions = 0
                    discover()
                }
            } catch(Exception e) {
                log.error(e.message, e)
            }

            executions++
            Thread.sleep(refreshEverySec * 1000)
        }

    }

}
