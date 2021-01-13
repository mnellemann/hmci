/*
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

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.Thread.*;

class Insights {

    private final static Logger log = LoggerFactory.getLogger(Insights.class);

    final Configuration configuration;

    InfluxClient influxClient;
    final Map<String, HmcClient> hmcClients = new HashMap<>();
    final Map<String,ManagedSystem> systems = new HashMap<>();
    final Map<String, LogicalPartition> partitions = new HashMap<>();


    Insights(Configuration configuration) {
        this.configuration = configuration;
        try {
            influxClient = new InfluxClient(configuration.influx);
            influxClient.login();
        } catch (Exception e) {
            System.exit(1);
        }

        // Initial scan
        discover();
    }


    void discover() {

        configuration.hmc.forEach( configHmc -> {
            if(!hmcClients.containsKey(configHmc.name)) {
                HmcClient hmcClient = new HmcClient(configHmc);
                hmcClients.put(configHmc.name, hmcClient);
                log.info("discover() - Adding HMC: " + hmcClient);
            }
        });

        hmcClients.forEach(( hmcId, hmcClient) -> {

            try {
                hmcClient.logoff();
                hmcClient.login();
                hmcClient.getManagedSystems().forEach((systemId, system) -> {

                    // Add to list of known systems
                    if(!systems.containsKey(systemId)) {
                        systems.put(systemId, system);
                        log.info(hmcId + " discover() - Found ManagedSystem: " + system);
                    }

                    // Get LPAR's for this system
                    try {
                        hmcClient.getLogicalPartitionsForManagedSystem(system).forEach((partitionId, partition) -> {

                            // Add to list of known partitions
                            if(!partitions.containsKey(partitionId)) {
                                partitions.put(partitionId, partition);
                                log.info(hmcId + " discover() - Found LogicalPartition: " + partition);
                            }

                        });
                    } catch (Exception e) {
                        log.error(hmcId + " discover() - getLogicalPartitions", e);
                    }

                });

            } catch(Exception e) {
                log.error(hmcId + " discover() - getManagedSystems: " + e.getMessage());
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
                    log.error("getMetricsForPartitions() - getPcmDataForLogicalPartition", e);
                }
                if(tmpJsonString2 != null && !tmpJsonString2.isEmpty()) {
                    partition.processMetrics(tmpJsonString2);
                }

            });

        } catch(Exception e) {
            log.error("getMetricsForPartitions()", e);
        }
    }


    void getMetricsForEnergy() {

        systems.forEach((systemId, system) -> {

            HmcClient hmcClient = hmcClients.get(system.hmcId);

            // Get and process metrics for this system
            String tmpJsonString = null;
            try {
                tmpJsonString = hmcClient.getPcmDataForEnergy(system.energy);
            } catch (Exception e) {
                log.error("getMetricsForEnergy()", e);
            }

            if(tmpJsonString != null && !tmpJsonString.isEmpty()) {
                system.energy.processMetrics(tmpJsonString);
            }

        });

    }


    void writeMetricsForManagedSystems() {
        try {
            systems.forEach((systemId, system) -> influxClient.writeManagedSystem(system));
        } catch (NullPointerException npe) {
            log.warn("writeMetricsForManagedSystems() - NPE: " + npe.toString(), npe);
        }
    }


    void writeMetricsForLogicalPartitions() {
        try {
            partitions.forEach((partitionId, partition) -> influxClient.writeLogicalPartition(partition));
        } catch (NullPointerException npe) {
            log.warn("writeMetricsForLogicalPartitions() - NPE: " + npe.toString(), npe);
        }
    }


    void writeMetricsForSystemEnergy() {
        try {
            systems.forEach((systemId, system) -> influxClient.writeSystemEnergy(system.energy));
        } catch (NullPointerException npe) {
            log.warn("writeMetricsForSystemEnergy() - NPE: " + npe.toString(), npe);
        }
    }


    void run() throws InterruptedException {

        log.debug("run()");
        int executions = 0;
        AtomicBoolean keepRunning = new AtomicBoolean(true);

        Thread shutdownHook = new Thread(() -> {
            keepRunning.set(false);
            System.out.println("Stopping HMCi, please wait ...");
        });
        Runtime.getRuntime().addShutdownHook(shutdownHook);

        do {
            Instant start = Instant.now();
            try {
                getMetricsForSystems();
                getMetricsForPartitions();
                getMetricsForEnergy();

                writeMetricsForManagedSystems();
                writeMetricsForLogicalPartitions();
                writeMetricsForSystemEnergy();
                influxClient.writeBatchPoints();

            } catch (Exception e) {
                log.error("run()", e);
            }

            // Refresh
            if (++executions > configuration.rescan) {
                executions = 0;
                discover();
            }

            Instant end = Instant.now();
            long timeSpend = Duration.between(start, end).getSeconds();
            log.debug("run() - duration sec: " + timeSpend);
            if(timeSpend < configuration.refresh) {
                //noinspection BusyWait
                sleep((configuration.refresh - timeSpend) * 1000);
            }

        } while (keepRunning.get());

    }

}
