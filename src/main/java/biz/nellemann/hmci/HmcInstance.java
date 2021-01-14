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

import biz.nellemann.hmci.Configuration.HmcObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.Thread.sleep;

class HmcInstance implements Runnable {

    private final static Logger log = LoggerFactory.getLogger(HmcInstance.class);

    private final String hmcId;
    private final Long updateValue;
    private final Long rescanValue;
    private final Map<String,ManagedSystem> systems = new HashMap<>();
    private final Map<String, LogicalPartition> partitions = new HashMap<>();

    private final HmcRestClient hmcRestClient;
    private final InfluxClient influxClient;
    private final AtomicBoolean keepRunning = new AtomicBoolean(true);


    HmcInstance(HmcObject configHmc, InfluxClient influxClient) {
        this.hmcId = configHmc.name;
        this.updateValue = configHmc.update;
        this.rescanValue = configHmc.rescan;
        this.influxClient = influxClient;
        hmcRestClient = new HmcRestClient(configHmc.url, configHmc.username, configHmc.password, configHmc.unsafe);
        log.debug(String.format("HmcInstance() - id: %s, update: %s, refresh %s", hmcId, updateValue, rescanValue));
    }


    @Override
    public String toString() {
        return hmcId;
    }


    @Override
    public void run() {

        log.debug("run() - " + hmcId);
        int executions = 0;

        discover();

        do {
            Instant instantStart = Instant.now();
            try {
                getMetricsForSystems();
                getMetricsForPartitions();
                getMetricsForEnergy();

                writeMetricsForManagedSystems();
                writeMetricsForLogicalPartitions();
                writeMetricsForSystemEnergy();
                influxClient.writeBatchPoints();

                // Refresh
                if (++executions > rescanValue) {
                    executions = 0;
                    discover();
                }

            } catch (Exception e) {
                log.error("run()", e);
            }

            Instant instantEnd = Instant.now();
            long timeSpend = Duration.between(instantStart, instantEnd).getSeconds();
            log.debug("run() - duration sec: " + timeSpend);
            if(timeSpend < updateValue) {
                try {
                    log.debug("run() - sleep sec: " + (updateValue - timeSpend));
                    //noinspection BusyWait
                    sleep((updateValue - timeSpend) * 1000);
                } catch (InterruptedException e) {
                    log.error("run() - sleep interrupted", e);
                }
            }

        } while (keepRunning.get());

    }


    void discover() {

        log.debug("discover() - " + hmcId);

        try {
            hmcRestClient.logoff();
            hmcRestClient.login();
            hmcRestClient.getManagedSystems().forEach((systemId, system) -> {

                // Add to list of known systems
                if(!systems.containsKey(systemId)) {
                    systems.put(systemId, system);
                    log.info("discover() - Found ManagedSystem: " + system + " @" + hmcId);
                }

                // Get LPAR's for this system
                try {
                    hmcRestClient.getLogicalPartitionsForManagedSystem(system).forEach((partitionId, partition) -> {

                        // Add to list of known partitions
                        if(!partitions.containsKey(partitionId)) {
                            partitions.put(partitionId, partition);
                            log.info("discover() - Found LogicalPartition: " + partition + " @" + hmcId);
                        }

                    });
                } catch (Exception e) {
                    log.error("discover() - getLogicalPartitions", e);
                }

            });

        } catch(Exception e) {
            log.error("discover() - getManagedSystems: " + e.getMessage());
        }

    }


    void getMetricsForSystems() {

        systems.forEach((systemId, system) -> {

            // Get and process metrics for this system
            String tmpJsonString = null;
            try {
                tmpJsonString = hmcRestClient.getPcmDataForManagedSystem(system);
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

                // Get and process metrics for this partition
                String tmpJsonString2 = null;
                try {
                    tmpJsonString2 = hmcRestClient.getPcmDataForLogicalPartition(partition);
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

            // Get and process metrics for this system
            String tmpJsonString = null;
            try {
                tmpJsonString = hmcRestClient.getPcmDataForEnergy(system.energy);
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


}
