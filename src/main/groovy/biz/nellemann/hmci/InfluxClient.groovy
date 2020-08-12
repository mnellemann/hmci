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
            influxDB = InfluxDBFactory.connect(url, username, password);
            createDatabase()
        }
    }

    void logoff() {
        influxDB?.close();
    }

    void createDatabase() {
        try {
            // Create a database...
            // https://docs.influxdata.com/influxdb/v1.7/query_language/database_management/
            influxDB.query(new Query("CREATE DATABASE " + database));
            influxDB.setDatabase(database);

            // ... and a retention policy, if necessary.
            // https://docs.influxdata.com/influxdb/v1.7/query_language/database_management/
            /*String retentionPolicyName = "one_day_only";
            influxDB.query(new Query("CREATE RETENTION POLICY " + retentionPolicyName
                    + " ON " + database + " DURATION 1d REPLICATION 1 DEFAULT"));
            influxDB.setRetentionPolicy(retentionPolicyName);*/

            // Enable batch writes to get better performance.
            influxDB.enableBatch(BatchOptions.DEFAULTS);
        } catch(Exception e) {
            log.error("createDatabase()", e)
        }

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


    void read() {
        // Query your data using InfluxQL.
        // https://docs.influxdata.com/influxdb/v1.7/query_language/data_exploration/#the-basic-select-statement
        QueryResult queryResult = influxDB.query(new Query("SELECT * FROM h2o_feet"));
        println(queryResult);
    }


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

        BatchPoints batchPoints = BatchPoints
                .database(database)
                //.retentionPolicy("defaultPolicy")
                .build();

        /*
        ServerProcessor processor
        ServerMemory memory
        PhysicalProcessorPool physicalProcessorPool
        SharedProcessorPool sharedProcessorPool

         + VIOS
         */

        getSystemMemory(system, timestamp).each {
            batchPoints.point(it)
        }

        getSystemProcessor(system, timestamp).each {
            batchPoints.point(it)
        }

        getSystemSharedProcessorPools(system, timestamp).each {
            batchPoints.point(it)
        }

        influxDB.write(batchPoints);
    }


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

        BatchPoints batchPoints = BatchPoints
                .database(database)
                .build();

        getPartitionMemory(partition, timestamp).each {
            batchPoints.point(it)
        }

        getPartitionProcessor(partition, timestamp).each {
            batchPoints.point(it)
        }

        getPartitionVirtualEthernetAdapter(partition, timestamp).each {
            batchPoints.point(it)
        }

        influxDB.write(batchPoints);
    }


    private static List<Point> getSystemMemory(ManagedSystem system, Instant timestamp) {

        Map map = system.getMemoryMetrics()
        List<Point> pointList = map.collect {fieldName, fieldValue ->

            return Point.measurement("SystemMemory")
                    .time(timestamp.toEpochMilli(), TimeUnit.MILLISECONDS)
                    .tag("system", system.name)
                    .tag("name", fieldName.capitalize()) // The dashboard expects it
                    .addField("value", fieldValue)
                    .build()
        }

        return pointList;
    }


    private static List<Point> getSystemProcessor(ManagedSystem system, Instant timestamp) {

        Map map = system.getProcessorMetrics()
        List<Point> pointList = map.collect {fieldName, fieldValue ->

            return Point.measurement("SystemProcessor")
                    .time(timestamp.toEpochMilli(), TimeUnit.MILLISECONDS)
                    .tag("system", system.name)
                    .tag("name", fieldName.capitalize()) // The dashboard expects it
                    .addField("value", fieldValue)
                    .build()
        }

        return pointList;
    }


    private static List<Point> getSystemSharedProcessorPools(ManagedSystem system, Instant timestamp) {

        List<Point> pointList
        system.getSharedProcessorPools().each {name, map ->
            //log.debug(name) // Pool name

            pointList = map.collect { fieldName, fieldValue ->

                return Point.measurement("SystemSharedProcessorPool")
                        .time(timestamp.toEpochMilli(), TimeUnit.MILLISECONDS)
                        .tag("system", system.name)
                        .tag("pool", name)
                        .tag("name", fieldName)
                        .addField("value", fieldValue)
                        .build()
            }

        }

        return pointList;
    }




    private static List<Point> getPartitionMemory(LogicalPartition partition, Instant timestamp) {

        Map map = partition.getMemoryMetrics()
        List<Point> pointList = map.collect {fieldName, fieldValue ->

            return Point.measurement("PartitionMemory")
                    .time(timestamp.toEpochMilli(), TimeUnit.MILLISECONDS)
                    .tag("partition", partition.name)
                    .tag("system", partition.system.name)
                    .tag("name", fieldName.capitalize()) // The dashboard expects it
                    .addField("value", fieldValue)
                    .build()
        }

        return pointList;
    }


    private static List<Point> getPartitionProcessor(LogicalPartition partition, Instant timestamp) {

        Map map = partition.getProcessorMetrics()
        List<Point> pointList = map.collect {fieldName, fieldValue ->

            return Point.measurement("PartitionProcessor")
                    .time(timestamp.toEpochMilli(), TimeUnit.MILLISECONDS)
                    .tag("partition", partition.name)
                    .tag("system", partition.system.name)
                    .tag("name", fieldName.capitalize()) // The dashboard expects it
                    .addField("value", fieldValue)
                    .build()
        }

        return pointList;
    }



    private static List<Point> getPartitionVirtualEthernetAdapter(LogicalPartition partition, Instant timestamp) {
        List<Map> metrics = partition.getVirtualEthernetAdapterMetrics()
        return processMeasurementMap(metrics, timestamp, "PartitionVirtualEthernetAdapters")
    }



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
