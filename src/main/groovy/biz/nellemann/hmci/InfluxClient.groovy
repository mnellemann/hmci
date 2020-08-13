package biz.nellemann.hmci


import groovy.util.logging.Slf4j
import org.influxdb.dto.BatchPoints

import java.time.Instant
import java.util.concurrent.TimeUnit
import org.influxdb.InfluxDB
import org.influxdb.BatchOptions
import org.influxdb.InfluxDBFactory
import org.influxdb.dto.QueryResult
import org.influxdb.dto.Query
import org.influxdb.dto.Point


@Slf4j
class InfluxClient {

    final String url
    final String username
    final String password
    final String database

    InfluxDB influxDB

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
            } catch(Exception e) {
                log.error(e.message)
                throw new Exception(e)
            }
        }
    }

    void logoff() {
        influxDB?.close();
    }


    void createDatabase() {
        // Create a database...
        influxDB.query(new Query("CREATE DATABASE " + database));
        influxDB.setDatabase(database);

        // ... and a retention policy, if necessary.
        /*
        String retentionPolicyName = "HMCI_ONE_YEAR";
        influxDB.query(new Query("CREATE RETENTION POLICY " + retentionPolicyName
                + " ON " + database + " DURATION 365d REPLICATION 1 DEFAULT"));
        influxDB.setRetentionPolicy(retentionPolicyName);*/

        // Enable batch writes to get better performance.
        influxDB.enableBatch(BatchOptions.DEFAULTS);
    }


    void write() {
        // Write points to InfluxDB.
        influxDB.write(Point.measurement("h2o_feet")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .tag("location", "santa_monica")
                .addField("level description", "below 3 feet")
                .addField("water_level", 2.064d)
                .build());

        influxDB.write(Point.measurement("h2o_feet")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .tag("location", "coyote_creek")
                .addField("level description", "between 6 and 9 feet")
                .addField("water_level", 8.12d)
                .build());

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

        BatchPoints batchPoints = BatchPoints.database(database).build();

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

        influxDB.write(batchPoints);
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

        BatchPoints batchPoints = BatchPoints.database(database).build();

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

        influxDB.write(batchPoints);
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

                Point.Builder builder = Point.measurement(measurement)
                        .time(timestamp.toEpochMilli(), TimeUnit.MILLISECONDS)
                        .tag("name", fieldName)
                        .addField("value", fieldValue)

                // For each field, we add all tags
                map.get("tags").each { String tagName, String tagValue ->
                    builder.tag(tagName, tagValue)
                }

                list.add(builder.build())
            }

        }

        return list
    }


}
