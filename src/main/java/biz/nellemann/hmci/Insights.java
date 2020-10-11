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
package biz.nellemann.hmci;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

class Insights {

    private final static Logger log = LoggerFactory.getLogger(Insights.class);

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
            if(hmcClients != null && !hmcClients.containsKey(configHmc.name)) {
                log.debug("Adding HMC: " + configHmc.toString());
                HmcClient hmcClient = new HmcClient(configHmc);
                hmcClients.put(configHmc.name, hmcClient);
            }
        });

        hmcClients.forEach(( hmcId, hmcClient) -> {

            try {
                hmcClient.login();
                hmcClient.getManagedSystems().forEach((systemId, system) -> {

                    // Add to list of known systems
                    systems.putIfAbsent(systemId, system);

                    // Get LPAR's for this system
                    try {
                        hmcClient.getLogicalPartitionsForManagedSystem(system).forEach((partitionId, partition) -> {

                            // Add to list of known partitions
                            partitions.putIfAbsent(partitionId, partition);
                        });
                    } catch (Exception e) {
                        log.error("discover()", e);
                    }

                });

            } catch(Exception e) {
                log.error("discover() - " + hmcId + " error: " + e.getMessage());
                //hmcClients.remove(hmcId);
            }

        });

    }


    void getMetricsForSystems() {

        systems.forEach((systemId, system) -> {

            HmcClient hmcClient = hmcClients.get(system.hmcId);

            // Get and process metrics for this system
            String tmpJsonString = null;
            try {
                tmpJsonString = hmcClient.getPcmDataForManagedSystem(system);
            } catch (Exception e) {
                log.error("getMetricsForSystems()", e);
                //e.printStackTrace();
            }

            if(tmpJsonString != null && !tmpJsonString.isEmpty()) {
                system.processMetrics(tmpJsonString);
            }

        });

    }


    void getMetricsForPartitions() {

        try {

            // Get LPAR's for this system
            partitions.forEach((partitionId, partition) -> {

                HmcClient hmcClient = hmcClients.get(partition.system.hmcId);

                // Get and process metrics for this partition
                String tmpJsonString2 = null;
                try {
                    tmpJsonString2 = hmcClient.getPcmDataForLogicalPartition(partition);
                } catch (Exception e) {
                    log.error("getMetricsForPartitions()", e);
                }
                if(tmpJsonString2 != null && !tmpJsonString2.isEmpty()) {
                    partition.processMetrics(tmpJsonString2);
                }

            });

        } catch(Exception e) {
            log.error("getMetricsForPartitions()", e);
        }
    }


    void writeMetricsForManagedSystems() {
        systems.forEach((systemId, system) -> {
            influxClient.writeManagedSystem(system);
        });
    }


    void writeMetricsForLogicalPartitions() {
        partitions.forEach((partitionId, partition) -> {
            influxClient.writeLogicalPartition(partition);
        });
    }


    void run() throws InterruptedException {

        log.debug("run()");
        int executions = 0;

        while(true) {

            try {
                getMetricsForSystems();
                getMetricsForPartitions();

                writeMetricsForManagedSystems();
                writeMetricsForLogicalPartitions();
                influxClient.writeBatchPoints();

                // Refresh HMC's
                if(executions > configuration.rescan) {
                    executions = 0;
                    discover();
                }
            } catch(Exception e) {
                log.error("run()", e.getMessage());
            }

            executions++;
            Thread.sleep(configuration.refresh * 1000);
        }

    }

}
