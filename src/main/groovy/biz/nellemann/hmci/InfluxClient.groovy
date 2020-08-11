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


        batchPoints.point(getSystemMemory(system, timestamp));
        batchPoints.point(getSystemProcessor(system, timestamp));

        influxDB.write(batchPoints);
    }



    private static Point getSystemMemory(ManagedSystem system, Instant timestamp) {

        Point.Builder point1Builder = Point.measurement("SystemMemory")
                .time(timestamp.toEpochMilli(), TimeUnit.MILLISECONDS)
                .tag("name", system.name)

        Map memoryMap = system.getMemoryMetrics()
        memoryMap.each {fieldName, fieldValue ->
            point1Builder.addField(fieldName, fieldValue)
        }

        return point1Builder.build();
    }


    private static Point getSystemProcessor(ManagedSystem system, Instant timestamp) {

        Point.Builder point1Builder = Point.measurement("SystemProcessor")
                .time(timestamp.toEpochMilli(), TimeUnit.MILLISECONDS)
                .tag("name", system.name)

        Map memoryMap = system.getProcessorMetrics()
        memoryMap.each {fieldName, fieldValue ->
            point1Builder.addField(fieldName, fieldValue)
        }

        return point1Builder.build();
    }

}
