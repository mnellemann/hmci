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

import groovy.util.logging.Slf4j
import org.influxdb.BatchOptions
import org.influxdb.InfluxDB
import org.influxdb.InfluxDBFactory
import org.influxdb.dto.BatchPoints
import org.influxdb.dto.Point
import org.influxdb.dto.Query

import java.time.Instant
import java.util.concurrent.TimeUnit

@Slf4j
class InfluxClient {

    final String url
    final String username
    final String password
    final String database

    InfluxDB influxDB
    BatchPoints batchPoints


    InfluxClient(String url, String username, String password, String database) {
        this.url = url
        this.username = username
        this.password = password
        this.database = database
    }


    void login() {
        if(!influxDB) {
            try {
                influxDB = InfluxDBFactory.connect(url, username, password);
                createDatabase()

                // Enable batch writes to get better performance.
                //BatchOptions options = BatchOptions.DEFAULTS.actions(300).flushDuration(500);
                influxDB.enableBatch(BatchOptions.DEFAULTS);
                //influxDB.setLogLevel(InfluxDB.LogLevel.BASIC);

                batchPoints = BatchPoints.database(database).precision(TimeUnit.SECONDS).build();

            } catch(Exception e) {
                log.error(e.message)
                throw new Exception(e)
            }
        }
    }


    void logoff() {
        influxDB?.close();
        influxDB = null
    }


    void createDatabase() {
        // Create our database... with a default retention of 156w == 3 years
        influxDB.query(new Query("CREATE DATABASE " + database + " WITH DURATION 156w"));
        influxDB.setDatabase(database);
    }


    void writeBatchPoints() {
        log.debug("writeBatchPoints()")
        try {
            influxDB.write(batchPoints);
        } catch(Exception e) {
            log.error("writeBatchPoints() error - " + e.message)
            logoff()
            login()
        }
    }



    /*
        Managed System
     */


    void writeManagedSystem(ManagedSystem system) {

        if(system.metrics == null) {
            log.warn("writeManagedSystem() - null metrics, skipping")
            return
        }

        Instant timestamp = system.getTimestamp()
        if(!timestamp) {
            log.warn("writeManagedSystem() - no timestamp, skipping")
            return
        }

        //BatchPoints batchPoints = BatchPoints.database(database).build();

        getSystemMemory(system, timestamp).each {
            batchPoints.point(it)
        }

        getSystemProcessor(system, timestamp).each {
            batchPoints.point(it)
        }

        getSystemSharedProcessorPools(system, timestamp).each {
            batchPoints.point(it)
        }

        getSystemSharedAdapters(system, timestamp).each {
            batchPoints.point(it)
        }

        getSystemFiberChannelAdapters(system, timestamp).each {
            batchPoints.point(it)
        }

        getSystemGenericPhysicalAdapters(system, timestamp).each {
            batchPoints.point(it)
        }

        getSystemGenericVirtualAdapters(system, timestamp).each {
            batchPoints.point(it)
        }
    }


    private static List<Point> getSystemMemory(ManagedSystem system, Instant timestamp) {
        List<Map> metrics = system.getMemoryMetrics()
        return processMeasurementMap(metrics, timestamp, "SystemMemory")
    }

    private static List<Point> getSystemProcessor(ManagedSystem system, Instant timestamp) {
        List<Map> metrics = system.getProcessorMetrics()
        return processMeasurementMap(metrics, timestamp, "SystemProcessor")
    }

    private static List<Point> getSystemSharedProcessorPools(ManagedSystem system, Instant timestamp) {
        List<Map> metrics = system.getSharedProcessorPools()
        return processMeasurementMap(metrics, timestamp, "SystemSharedProcessorPool")
    }

    private static List<Point> getSystemSharedAdapters(ManagedSystem system, Instant timestamp) {
        List<Map> metrics = system.getSystemSharedAdapters()
        return processMeasurementMap(metrics, timestamp, "SystemSharedAdapters")
    }

    private static List<Point> getSystemFiberChannelAdapters(ManagedSystem system, Instant timestamp) {
        List<Map> metrics = system.getSystemFiberChannelAdapters()
        return processMeasurementMap(metrics, timestamp, "SystemFiberChannelAdapters")
    }

    private static List<Point> getSystemGenericPhysicalAdapters(ManagedSystem system, Instant timestamp) {
        List<Map> metrics = system.getSystemGenericPhysicalAdapters()
        return processMeasurementMap(metrics, timestamp, "SystemGenericPhysicalAdapters")
    }

    private static List<Point> getSystemGenericVirtualAdapters(ManagedSystem system, Instant timestamp) {
        List<Map> metrics = system.getSystemGenericVirtualAdapters()
        return processMeasurementMap(metrics, timestamp, "SystemGenericVirtualAdapters")
    }


    /*
        Logical Partitions
     */

    void writeLogicalPartition(LogicalPartition partition) {

        if(partition.metrics == null) {
            log.warn("writeLogicalPartition() - null metrics, skipping")
            return
        }

        Instant timestamp = partition.getTimestamp()
        if(!timestamp) {
            log.warn("writeLogicalPartition() - no timestamp, skipping")
            return
        }

        //BatchPoints batchPoints = BatchPoints.database(database).build();

        getPartitionAffinityScore(partition, timestamp).each {
            batchPoints.point(it)
        }

        getPartitionMemory(partition, timestamp).each {
            batchPoints.point(it)
        }

        getPartitionProcessor(partition, timestamp).each {
            batchPoints.point(it)
        }

        getPartitionVirtualEthernetAdapter(partition, timestamp).each {
            batchPoints.point(it)
        }

        getPartitionVirtualFiberChannelAdapter(partition, timestamp).each {
            batchPoints.point(it)
        }

        //influxDB.write(batchPoints);
    }

    private static List<Point> getPartitionAffinityScore(LogicalPartition partition, Instant timestamp) {
        List<Map> metrics = partition.getAffinityScore()
        return processMeasurementMap(metrics, timestamp, "PartitionAffinityScore")
    }

    private static List<Point> getPartitionMemory(LogicalPartition partition, Instant timestamp) {
        List<Map> metrics = partition.getMemoryMetrics()
        return processMeasurementMap(metrics, timestamp, "PartitionMemory")
    }

    private static List<Point> getPartitionProcessor(LogicalPartition partition, Instant timestamp) {
        List<Map> metrics = partition.getProcessorMetrics()
        return processMeasurementMap(metrics, timestamp, "PartitionProcessor")
    }

    private static List<Point> getPartitionVirtualEthernetAdapter(LogicalPartition partition, Instant timestamp) {
        List<Map> metrics = partition.getVirtualEthernetAdapterMetrics()
        return processMeasurementMap(metrics, timestamp, "PartitionVirtualEthernetAdapters")
    }

    private static List<Point> getPartitionVirtualFiberChannelAdapter(LogicalPartition partition, Instant timestamp) {
        List<Map> metrics = partition.getVirtualFiberChannelAdaptersMetrics()
        return processMeasurementMap(metrics, timestamp, "PartitionVirtualFiberChannelAdapters")
    }



    /*
        Shared
     */

    private static List<Point> processMeasurementMap(List<Map> listOfMaps, Instant timestamp, String measurement) {

        List<Point> list = new ArrayList<>()

        listOfMaps.each { map ->

            // Iterate fields
            map.get("fields").each { String fieldName, BigDecimal fieldValue ->
                log.debug("processMeasurementMap() " + measurement + " - fieldName: " + fieldName + ", fieldValue: " + fieldValue)

                Point.Builder builder = Point.measurement(measurement)
                        .time(timestamp.toEpochMilli(), TimeUnit.MILLISECONDS)
                        .tag("name", fieldName)
                        .addField("value", fieldValue)

                // For each field, we add all tags
                map.get("tags").each { String tagName, String tagValue ->
                    builder.tag(tagName, tagValue)
                    log.debug("processMeasurementMap() " + measurement + " - tagName: " + tagName + ", tagValue: " + tagValue)
                }

                list.add(builder.build())
            }

        }

        return list
    }


}
