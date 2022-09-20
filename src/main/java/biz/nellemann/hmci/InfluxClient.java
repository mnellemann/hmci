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
import org.influxdb.dto.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;

public final class InfluxClient {

    private final static Logger log = LoggerFactory.getLogger(InfluxClient.class);

    final private String url;
    final private String username;
    final private String password;
    final private String database;

    private InfluxDB influxDB;


    InfluxClient(InfluxObject config) {
        this.url = config.url;
        this.username = config.username;
        this.password = config.password;
        this.database = config.database;
    }


    synchronized void login() throws RuntimeException, InterruptedException {

        if(influxDB != null) {
            return;
        }

        boolean connected = false;
        int loginErrors = 0;

        do {
            try {
                log.debug("Connecting to InfluxDB - {}", url);
                influxDB = InfluxDBFactory.connect(url, username, password).setDatabase(database);
                influxDB.version(); // This ensures that we actually try to connect to the db

                influxDB.enableBatch(
                    BatchOptions.DEFAULTS
                        .threadFactory(runnable -> {
                            Thread thread = new Thread(runnable);
                            thread.setDaemon(true);
                            return thread;
                        })
                );                                                                        // (4)

                Runtime.getRuntime().addShutdownHook(new Thread(influxDB::close));

                connected = true;
            } catch(Exception e) {
                sleep(15 * 1000);
                if(loginErrors++ > 3) {
                    log.error("login() - error, giving up: {}", e.getMessage());
                    throw new RuntimeException(e);
                } else {
                    log.warn("login() - error, retrying: {}", e.getMessage());
                }
            }
        } while(!connected);

    }


    synchronized void logoff() {
        if(influxDB != null) {
            influxDB.close();
        }
        influxDB = null;
    }

/*
    synchronized void writeBatchPoints() throws Exception {
        log.trace("writeBatchPoints()");
        try {
            influxDB.write(batchPoints);
            batchPoints = BatchPoints.database(database).precision(TimeUnit.SECONDS).build();
            errorCounter = 0;
        } catch (InfluxDBException.DatabaseNotFoundException e) {
            log.error("writeBatchPoints() - database \"{}\" not found/created: can't write data", database);
            if (++errorCounter > 3) {
                throw new RuntimeException(e);
            }
        } catch (org.influxdb.InfluxDBIOException e) {
            log.warn("writeBatchPoints() - io exception: {}", e.getMessage());
            if(++errorCounter < 3) {
                log.warn("writeBatchPoints() - reconnecting to InfluxDB due to io exception.");
                logoff();
                login();
                writeBatchPoints();
            } else {
                throw new RuntimeException(e);
            }
        } catch(Exception e) {
            log.warn("writeBatchPoints() - general exception:  {}", e.getMessage());
            if(++errorCounter < 3) {
                log.warn("writeBatchPoints() - reconnecting to InfluxDB due to general exception.");
                logoff();
                login();
                writeBatchPoints();
            } else {
                throw new RuntimeException(e);
            }
        }
    }
*/


    /*
        Managed System
     */


    void writeManagedSystem(ManagedSystem system) {

        if(system.metrics == null) {
            log.trace("writeManagedSystem() - null metrics, skipping: {}", system.name);
            return;
        }

        Instant timestamp = system.getTimestamp();
        if(timestamp == null) {
            log.warn("writeManagedSystem() - no timestamp, skipping: {}", system.name);
            return;
        }

        //getSystemDetails(system, timestamp).forEach( it -> batchPoints.point(it) );
        getSystemDetails(system, timestamp).forEach( it -> influxDB.write(it));
        //getSystemProcessor(system, timestamp).forEach( it -> batchPoints.point(it) );
        getSystemProcessor(system, timestamp).forEach( it -> influxDB.write(it) );
        //getSystemPhysicalProcessorPool(system, timestamp).forEach( it -> batchPoints.point(it) );
        getSystemPhysicalProcessorPool(system, timestamp).forEach( it -> influxDB.write(it) );
        //getSystemSharedProcessorPools(system, timestamp).forEach( it -> batchPoints.point(it) );
        getSystemSharedProcessorPools(system, timestamp).forEach( it -> influxDB.write(it) );
        //getSystemMemory(system, timestamp).forEach( it -> batchPoints.point(it) );
        getSystemMemory(system, timestamp).forEach( it -> influxDB.write(it) );

        //getSystemViosDetails(system, timestamp).forEach(it -> batchPoints.point(it) );
        getSystemViosDetails(system, timestamp).forEach(it -> influxDB.write(it) );
        //getSystemViosProcessor(system, timestamp).forEach( it -> batchPoints.point(it) );
        getSystemViosProcessor(system, timestamp).forEach( it -> influxDB.write(it) );
        //getSystemViosMemory(system, timestamp).forEach( it -> batchPoints.point(it) );
        getSystemViosMemory(system, timestamp).forEach( it -> influxDB.write(it) );

        //getSystemViosNetworkLpars(system, timestamp).forEach(it -> batchPoints.point(it) );
        getSystemViosNetworkLpars(system, timestamp).forEach(it -> influxDB.write(it) );
        //getSystemViosNetworkGenericAdapters(system, timestamp).forEach(it -> batchPoints.point(it) );
        getSystemViosNetworkGenericAdapters(system, timestamp).forEach(it -> influxDB.write(it) );
        //getSystemViosNetworkSharedAdapters(system, timestamp).forEach(it -> batchPoints.point(it) );
        getSystemViosNetworkSharedAdapters(system, timestamp).forEach(it -> influxDB.write(it) );
        //getSystemViosNetworkVirtualAdapters(system, timestamp).forEach(it -> batchPoints.point(it) );
        getSystemViosNetworkVirtualAdapters(system, timestamp).forEach(it -> influxDB.write(it) );

        //getSystemViosStorageLpars(system, timestamp).forEach(it -> batchPoints.point(it) );
        getSystemViosStorageLpars(system, timestamp).forEach(it -> influxDB.write(it) );
        //getSystemViosFiberChannelAdapters(system, timestamp).forEach(it -> batchPoints.point(it) );
        getSystemViosFiberChannelAdapters(system, timestamp).forEach(it -> influxDB.write(it) );
        //getSystemViosStoragePhysicalAdapters(system, timestamp).forEach(it -> batchPoints.point(it) );
        getSystemViosStoragePhysicalAdapters(system, timestamp).forEach(it -> influxDB.write(it) );
        //getSystemViosStorageVirtualAdapters(system, timestamp).forEach(it -> batchPoints.point(it) );
        getSystemViosStorageVirtualAdapters(system, timestamp).forEach(it -> influxDB.write(it) );

    }


    // TODO: server_details

    private static List<Point> getSystemDetails(ManagedSystem system, Instant timestamp) {
        List<Measurement> metrics = system.getDetails();
        return processMeasurementMap(metrics, timestamp, "server_details");
    }

    private static List<Point> getSystemProcessor(ManagedSystem system, Instant timestamp) {
        List<Measurement> metrics = system.getProcessorMetrics();
        return processMeasurementMap(metrics, timestamp, "server_processor");
    }

    private static List<Point> getSystemPhysicalProcessorPool (ManagedSystem system, Instant timestamp) {
        List<Measurement> metrics = system.getPhysicalProcessorPool();
        return processMeasurementMap(metrics, timestamp, "server_physicalProcessorPool");
    }

    private static List<Point> getSystemSharedProcessorPools(ManagedSystem system, Instant timestamp) {
        List<Measurement> metrics = system.getSharedProcessorPools();
        return processMeasurementMap(metrics, timestamp, "server_sharedProcessorPool");
    }

    private static List<Point> getSystemMemory(ManagedSystem system, Instant timestamp) {
        List<Measurement> metrics = system.getMemoryMetrics();
        return processMeasurementMap(metrics, timestamp, "server_memory");
    }

    private static List<Point> getSystemViosDetails(ManagedSystem system, Instant timestamp) {
        List<Measurement> metrics = system.getViosDetails();
        return processMeasurementMap(metrics, timestamp, "vios_details");
    }

    private static List<Point> getSystemViosProcessor(ManagedSystem system, Instant timestamp) {
        List<Measurement> metrics = system.getViosProcessorMetrics();
        return processMeasurementMap(metrics, timestamp, "vios_processor");
    }

    private static List<Point> getSystemViosMemory(ManagedSystem system, Instant timestamp) {
        List<Measurement> metrics = system.getViosMemoryMetrics();
        return processMeasurementMap(metrics, timestamp, "vios_memory");
    }

    private static List<Point> getSystemViosNetworkLpars(ManagedSystem system, Instant timestamp) {
        List<Measurement> metrics = system.getViosNetworkLpars();
        return processMeasurementMap(metrics, timestamp, "vios_network_lpars");
    }

    private static List<Point> getSystemViosNetworkVirtualAdapters(ManagedSystem system, Instant timestamp) {
        List<Measurement> metrics = system.getViosNetworkVirtualAdapters();
        return processMeasurementMap(metrics, timestamp, "vios_network_virtual");
    }

    private static List<Point> getSystemViosNetworkSharedAdapters(ManagedSystem system, Instant timestamp) {
        List<Measurement> metrics = system.getViosNetworkSharedAdapters();
        return processMeasurementMap(metrics, timestamp, "vios_network_shared");
    }

    private static List<Point> getSystemViosNetworkGenericAdapters(ManagedSystem system, Instant timestamp) {
        List<Measurement> metrics = system.getViosNetworkGenericAdapters();
        return processMeasurementMap(metrics, timestamp, "vios_network_generic");
    }


    private static List<Point> getSystemViosStorageLpars(ManagedSystem system, Instant timestamp) {
        List<Measurement> metrics = system.getViosStorageLpars();
        return processMeasurementMap(metrics, timestamp, "vios_storage_lpars");
    }

    private static List<Point> getSystemViosFiberChannelAdapters(ManagedSystem system, Instant timestamp) {
        List<Measurement> metrics = system.getViosStorageFiberChannelAdapters();
        return processMeasurementMap(metrics, timestamp, "vios_storage_FC");
    }

    private static List<Point> getSystemViosSharedStoragePools(ManagedSystem system, Instant timestamp) {
        List<Measurement> metrics = system.getViosStorageSharedStoragePools();
        return processMeasurementMap(metrics, timestamp, "vios_storage_SSP");
    }

    private static List<Point> getSystemViosStoragePhysicalAdapters(ManagedSystem system, Instant timestamp) {
        List<Measurement> metrics = system.getViosStoragePhysicalAdapters();
        return processMeasurementMap(metrics, timestamp, "vios_storage_physical");
    }

    private static List<Point> getSystemViosStorageVirtualAdapters(ManagedSystem system, Instant timestamp) {
        List<Measurement> metrics = system.getViosStorageVirtualAdapters();
        return processMeasurementMap(metrics, timestamp, "vios_storage_vFC");
    }




    /*
        Logical Partitions
     */

    void writeLogicalPartition(LogicalPartition partition) {

        if(partition.metrics == null) {
            log.warn("writeLogicalPartition() - null metrics, skipping: {}", partition.name);
            return;
        }

        Instant timestamp = partition.getTimestamp();
        if(timestamp == null) {
            log.warn("writeLogicalPartition() - no timestamp, skipping: {}", partition.name);
            return;
        }

        //getPartitionDetails(partition, timestamp).forEach( it -> batchPoints.point(it));
        getPartitionDetails(partition, timestamp).forEach( it -> influxDB.write(it));
        //getPartitionMemory(partition, timestamp).forEach( it -> batchPoints.point(it));
        getPartitionMemory(partition, timestamp).forEach( it -> influxDB.write(it));
        //getPartitionProcessor(partition, timestamp).forEach( it -> batchPoints.point(it));
        getPartitionProcessor(partition, timestamp).forEach( it -> influxDB.write(it));
        //getPartitionNetworkVirtual(partition, timestamp).forEach(it -> batchPoints.point(it));
        getPartitionNetworkVirtual(partition, timestamp).forEach(it -> influxDB.write(it));
        getPartitionSriovLogicalPorts(partition, timestamp).forEach(it -> influxDB.write(it));
        //getPartitionStorageVirtualGeneric(partition, timestamp).forEach(it -> batchPoints.point(it));
        getPartitionStorageVirtualGeneric(partition, timestamp).forEach(it -> influxDB.write(it));
        //getPartitionStorageVirtualFibreChannel(partition, timestamp).forEach(it -> batchPoints.point(it));
        getPartitionStorageVirtualFibreChannel(partition, timestamp).forEach(it -> influxDB.write(it));

    }

    private static List<Point> getPartitionDetails(LogicalPartition partition, Instant timestamp) {
        List<Measurement> metrics = partition.getDetails();
        return processMeasurementMap(metrics, timestamp, "lpar_details");
    }

    private static List<Point> getPartitionProcessor(LogicalPartition partition, Instant timestamp) {
        List<Measurement> metrics = partition.getProcessorMetrics();
        return processMeasurementMap(metrics, timestamp, "lpar_processor");
    }

    private static List<Point> getPartitionMemory(LogicalPartition partition, Instant timestamp) {
        List<Measurement> metrics = partition.getMemoryMetrics();
        return processMeasurementMap(metrics, timestamp, "lpar_memory");
    }

    private static List<Point> getPartitionNetworkVirtual(LogicalPartition partition, Instant timestamp) {
        List<Measurement> metrics = partition.getVirtualEthernetAdapterMetrics();
        return processMeasurementMap(metrics, timestamp, "lpar_net_virtual");   // Not 'network'
    }

    private static List<Point> getPartitionSriovLogicalPorts(LogicalPartition partition, Instant timestamp) {
        List<Measurement> metrics = partition.getSriovLogicalPorts();
        return processMeasurementMap(metrics, timestamp, "lpar_net_sriov");   // Not 'network'
    }

    // TODO: lpar_net_sriov

    private static List<Point> getPartitionStorageVirtualGeneric(LogicalPartition partition, Instant timestamp) {
        List<Measurement> metrics = partition.getVirtualGenericAdapterMetrics();
        return processMeasurementMap(metrics, timestamp, "lpar_storage_virtual");
    }

    private static List<Point> getPartitionStorageVirtualFibreChannel(LogicalPartition partition, Instant timestamp) {
        List<Measurement> metrics = partition.getVirtualFibreChannelAdapterMetrics();
        return processMeasurementMap(metrics, timestamp, "lpar_storage_vFC");
    }


    /*
        System Energy
        Not supported on older HMC (pre v8) or older Power server (pre Power 8)
     */


    void writeSystemEnergy(SystemEnergy systemEnergy) {

        if(systemEnergy.metrics == null) {
            log.trace("writeSystemEnergy() - null metrics, skipping: {}", systemEnergy.system.name);
            return;
        }

        Instant timestamp = systemEnergy.getTimestamp();
        if(timestamp == null) {
            log.warn("writeSystemEnergy() - no timestamp, skipping: {}", systemEnergy.system.name);
            return;
        }

        //getSystemEnergyPower(systemEnergy, timestamp).forEach(it -> batchPoints.point(it) );
        getSystemEnergyPower(systemEnergy, timestamp).forEach(it -> influxDB.write(it) );
        //getSystemEnergyTemperature(systemEnergy, timestamp).forEach(it -> batchPoints.point(it) );
        getSystemEnergyTemperature(systemEnergy, timestamp).forEach(it -> influxDB.write(it) );
    }

    private static List<Point> getSystemEnergyPower(SystemEnergy system, Instant timestamp) {
        List<Measurement> metrics = system.getPowerMetrics();
        return processMeasurementMap(metrics, timestamp, "server_energy_power");
    }

    private static List<Point> getSystemEnergyTemperature(SystemEnergy system, Instant timestamp) {
        List<Measurement> metrics = system.getThermalMetrics();
        return processMeasurementMap(metrics, timestamp, "server_energy_thermal");
    }


    /*
        Shared
     */

    private static List<Point> processMeasurementMap(List<Measurement> measurements, Instant timestamp, String measurement) {

        List<Point> listOfPoints = new ArrayList<>();
        measurements.forEach( m -> {

            Point.Builder builder = Point.measurement(measurement)
                .time(timestamp.getEpochSecond(), TimeUnit.SECONDS);

            // Iterate fields
            m.fields.forEach((fieldName, fieldValue) ->  {

                log.trace("processMeasurementMap() {} - fieldName: {}, fieldValue: {}", measurement, fieldName, fieldValue);
                if(fieldValue instanceof Number) {
                    Number num = (Number) fieldValue;
                    builder.addField(fieldName, num);
                } else if(fieldValue instanceof Boolean) {
                    Boolean bol = (Boolean) fieldValue;
                    builder.addField(fieldName, bol);
                } else {
                    String str = (String) fieldValue;
                    builder.addField(fieldName, str);
                }
            });

            // Iterate sorted tags
            Map<String, String> sortedTags = new TreeMap<String, String>(m.tags);
            sortedTags.forEach((tagName, tagValue) -> {
                log.trace("processMeasurementMap() {} - tagName: {}, tagValue: {}", measurement, tagName, tagValue);
                builder.tag(tagName, tagValue);
            });

            listOfPoints.add(builder.build());

        });

        return listOfPoints;
    }

}
