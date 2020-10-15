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

import biz.nellemann.hmci.Configuration.InfluxObject;
import org.influxdb.BatchOptions;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

class InfluxClient {

    private final static Logger log = LoggerFactory.getLogger(InfluxClient.class);

    final private String url;
    final private String username;
    final private String password;
    final private String database;

    private InfluxDB influxDB;
    private BatchPoints batchPoints;


    InfluxClient(InfluxObject config) {
        this.url = config.url;
        this.username = config.username;
        this.password = config.password;
        this.database = config.database;
    }


    synchronized void login() throws Exception {

        if(influxDB != null) {
            return;
        }

        try {
            log.debug("Connecting to InfluxDB - " + url);
            influxDB = InfluxDBFactory.connect(url, username, password);
            createDatabase();

            // Enable batch writes to get better performance.
            BatchOptions options = BatchOptions.DEFAULTS.actions(1000).flushDuration(5000).precision(TimeUnit.SECONDS);
            influxDB.enableBatch(options);
            batchPoints = BatchPoints.database(database).precision(TimeUnit.SECONDS).build();

        } catch(Exception e) {
            log.error("login() error - " + e.getMessage());
            throw new Exception(e);
        }
    }


    synchronized void logoff() {
        if(influxDB != null) {
            influxDB.close();
        }
        influxDB = null;
    }


    void createDatabase() {
        // Create our database... with a default retention of 156w == 3 years
        influxDB.query(new Query("CREATE DATABASE " + database + " WITH DURATION 156w"));
        influxDB.setDatabase(database);
    }


    synchronized void writeBatchPoints() throws Exception {
        log.debug("writeBatchPoints()");
        try {
            influxDB.write(batchPoints);
        } catch(Exception e) {
            log.error("writeBatchPoints() error - " + e.getMessage());
            logoff();
            login();
        }
    }



    /*
        Managed System
     */


    void writeManagedSystem(ManagedSystem system) {

        if(system.metrics == null) {
            log.warn("writeManagedSystem() - null metrics, skipping");
            return;
        }

        Instant timestamp = system.getTimestamp();
        if(timestamp == null) {
            log.warn("writeManagedSystem() - no timestamp, skipping");
            return;
        }

        getSystemMemory(system, timestamp).forEach( it -> batchPoints.point(it) );

        getSystemProcessor(system, timestamp).forEach( it -> batchPoints.point(it) );

        getSystemSharedProcessorPools(system, timestamp).forEach( it -> batchPoints.point(it) );

        getSystemSharedAdapters(system, timestamp).forEach( it -> batchPoints.point(it) );

        getSystemFiberChannelAdapters(system, timestamp).forEach( it -> batchPoints.point(it) );

        getSystemGenericPhysicalAdapters(system, timestamp).forEach( it -> batchPoints.point(it) );

        getSystemGenericVirtualAdapters(system, timestamp).forEach( it -> batchPoints.point(it) );

    }


    private static List<Point> getSystemMemory(ManagedSystem system, Instant timestamp) {
        List<Measurement> metrics = system.getMemoryMetrics();
        return processMeasurementMap(metrics, timestamp, "SystemMemory");
    }

    private static List<Point> getSystemProcessor(ManagedSystem system, Instant timestamp) {
        List<Measurement> metrics = system.getProcessorMetrics();
        return processMeasurementMap(metrics, timestamp, "SystemProcessor");
    }

    private static List<Point> getSystemSharedProcessorPools(ManagedSystem system, Instant timestamp) {
        List<Measurement> metrics = system.getSharedProcessorPools();
        return processMeasurementMap(metrics, timestamp, "SystemSharedProcessorPool");
    }

    private static List<Point> getSystemSharedAdapters(ManagedSystem system, Instant timestamp) {
        List<Measurement> metrics = system.getSystemSharedAdapters();
        return processMeasurementMap(metrics, timestamp, "SystemSharedAdapters");
    }

    private static List<Point> getSystemFiberChannelAdapters(ManagedSystem system, Instant timestamp) {
        List<Measurement> metrics = system.getSystemFiberChannelAdapters();
        return processMeasurementMap(metrics, timestamp, "SystemFiberChannelAdapters");
    }

    private static List<Point> getSystemGenericPhysicalAdapters(ManagedSystem system, Instant timestamp) {
        List<Measurement> metrics = system.getSystemGenericPhysicalAdapters();
        return processMeasurementMap(metrics, timestamp, "SystemGenericPhysicalAdapters");
    }

    private static List<Point> getSystemGenericVirtualAdapters(ManagedSystem system, Instant timestamp) {
        List<Measurement> metrics = system.getSystemGenericVirtualAdapters();
        return processMeasurementMap(metrics, timestamp, "SystemGenericVirtualAdapters");
    }


    /*
        Logical Partitions
     */

    void writeLogicalPartition(LogicalPartition partition) {

        if(partition.metrics == null) {
            log.warn("writeLogicalPartition() - null metrics, skipping");
            return;
        }

        Instant timestamp = partition.getTimestamp();
        if(timestamp == null) {
            log.warn("writeLogicalPartition() - no timestamp, skipping");
            return;
        }

        getPartitionAffinityScore(partition, timestamp).forEach( it -> batchPoints.point(it));

        getPartitionMemory(partition, timestamp).forEach( it -> batchPoints.point(it));

        getPartitionProcessor(partition, timestamp).forEach( it -> batchPoints.point(it));

        getPartitionVirtualEthernetAdapter(partition, timestamp).forEach( it -> batchPoints.point(it));

        getPartitionVirtualFiberChannelAdapter(partition, timestamp).forEach( it -> batchPoints.point(it));

    }
    
    private static List<Point> getPartitionAffinityScore(LogicalPartition partition, Instant timestamp) {
        List<Measurement> metrics = partition.getAffinityScore();
        return processMeasurementMap(metrics, timestamp, "PartitionAffinityScore");
    }

    private static List<Point> getPartitionMemory(LogicalPartition partition, Instant timestamp) {
        List<Measurement> metrics = partition.getMemoryMetrics();
        return processMeasurementMap(metrics, timestamp, "PartitionMemory");
    }

    private static List<Point> getPartitionProcessor(LogicalPartition partition, Instant timestamp) {
        List<Measurement> metrics = partition.getProcessorMetrics();
        return processMeasurementMap(metrics, timestamp, "PartitionProcessor");
    }

    private static List<Point> getPartitionVirtualEthernetAdapter(LogicalPartition partition, Instant timestamp) {
        List<Measurement> metrics = partition.getVirtualEthernetAdapterMetrics();
        return processMeasurementMap(metrics, timestamp, "PartitionVirtualEthernetAdapters");
    }

    private static List<Point> getPartitionVirtualFiberChannelAdapter(LogicalPartition partition, Instant timestamp) {
        List<Measurement> metrics = partition.getVirtualFiberChannelAdaptersMetrics();
        return processMeasurementMap(metrics, timestamp, "PartitionVirtualFiberChannelAdapters");
    }



    /*
        Shared
     */

    private static List<Point> processMeasurementMap(List<Measurement> measurements, Instant timestamp, String measurement) {

        List<Point> listOfPoints = new ArrayList<>();
        measurements.forEach( m -> {

            // Iterate fields
            //Map<String, BigDecimal> fieldsMap = m.get("fields");
            m.fields.forEach((fieldName, fieldValue) ->  {
                log.debug("processMeasurementMap() " + measurement + " - fieldName: " + fieldName + ", fieldValue: " + fieldValue);

                Point.Builder builder = Point.measurement(measurement)
                        .time(timestamp.toEpochMilli(), TimeUnit.MILLISECONDS)
                        .tag("name", fieldName)
                        .addField("value", fieldValue);

                // For each field, we add all tags
                //Map<String, String> tagsMap = m.get("tags");
                m.tags.forEach((tagName, tagValue) -> {
                    builder.tag(tagName, tagValue);
                    log.debug("processMeasurementMap() " + measurement + " - tagName: " + tagName + ", tagValue: " + tagValue);
                });

                listOfPoints.add(builder.build());
            });

        });

        return listOfPoints;
    }


}
