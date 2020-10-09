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

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j;

@Slf4j
@CompileStatic
class Insights {

    final Configuration configuration;

    InfluxClient influxClient;
    Map<String, HmcClient> hmcClients = new HashMap<>();
    Map<String,ManagedSystem> systems = new HashMap<String, ManagedSystem>();
    Map<String, LogicalPartition> partitions = new HashMap<String, LogicalPartition>();


    Insights(Configuration configuration) {
        this.configuration = configuration;

        try {
            influxClient = new InfluxClient(configuration.influx);
            influxClient.login();
        } catch(Exception e) {
            System.exit(1);
        }

        // Initial scan
        discover();
    }


    void discover() {

        configuration.hmc.forEach( configHmc -> {
            if(!hmcClients?.containsKey(configHmc.name)) {
                log.debug("Adding HMC: " + configHmc.toString())
                HmcClient hmcClient = new HmcClient(configHmc)
                hmcClients.put(configHmc.name, hmcClient)
            }
        });

        hmcClients.forEach(( hmcId, hmcClient) -> {

            try {
                hmcClient.login()
                hmcClient.getManagedSystems().each { systemId, system ->

                    // Add to list of known systems
                    systems.putIfAbsent(systemId, system)

                    // Get LPAR's for this system
                    hmcClient.getLogicalPartitionsForManagedSystem(system).forEach((partitionId, partition) -> {

                        // Add to list of known partitions
                        partitions.putIfAbsent(partitionId, partition)
                    });
                }
            } catch(Exception e) {
                log.error("discover() - " + hmcId + " error: " + e.message)
                //hmcClients.remove(hmcId)
            }

        });

    }


    void getMetricsForSystems() {

        try {

            systems.forEach((systemId, system) -> {

                HmcClient hmcClient = hmcClients.get(system.hmcId)

                // Get and process metrics for this system
                String tmpJsonString = hmcClient.getPcmDataForManagedSystem(system)
                if(tmpJsonString && !tmpJsonString.empty) {
                    system.processMetrics(tmpJsonString)
                }

            });

        } catch(Exception e) {
            log.error(e.message)
        }

    }


    void getMetricsForPartitions() {

        try {

            // Get LPAR's for this system
            partitions.forEach((partitionId, partition) -> {

                HmcClient hmcClient = hmcClients.get(partition.system.hmcId)

                // Get and process metrics for this partition
                String tmpJsonString2 = hmcClient.getPcmDataForLogicalPartition(partition)
                if(tmpJsonString2 && !tmpJsonString2.empty) {
                    partition.processMetrics(tmpJsonString2)
                }

            });

        } catch(Exception e) {
            log.error(e.message)
        }
    }


    void writeMetricsForManagedSystems() {
        systems.forEach((systemId, system) -> {
            influxClient.writeManagedSystem(system)
        });
    }


    void writeMetricsForLogicalPartitions() {
        partitions.each {partitionId, partition ->
            influxClient.writeLogicalPartition(partition)
        }
    }


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
                if(executions > configuration.rescan) {
                    executions = 0
                    discover()
                }
            } catch(Exception e) {
                log.error(e.message, e)
            }

            executions++
            Thread.sleep(configuration.refresh * 1000)
        }

    }

}
