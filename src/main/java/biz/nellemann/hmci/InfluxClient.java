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

import biz.nellemann.hmci.dto.toml.InfluxConfiguration;
import org.influxdb.BatchOptions;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;

public final class InfluxClient {

    private final static Logger log = LoggerFactory.getLogger(InfluxClient.class);

    final private String url;
    final private String username;
    final private String password;
    final private String database;

    private InfluxDB influxDB;

    InfluxClient(InfluxConfiguration config) {
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
                        .flushDuration(5000)
                        .threadFactory(runnable -> {
                            Thread thread = new Thread(runnable);
                            thread.setDaemon(true);
                            return thread;
                        })
                );
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
    public void write(List<Measurement> measurements, Instant timestamp, String name) {
        log.debug("write() - measurement: {} {}", name, measurements.size());
        processMeasurementMap(measurements, timestamp, name).forEach( (point) -> { influxDB.write(point); });
    }*/


    public void write(List<Measurement> measurements, String name) {
        log.debug("write() - measurement: {} {}", name, measurements.size());
        if(!measurements.isEmpty()) {
            processMeasurementMap(measurements, name).forEach((point) -> {
                influxDB.write(point);
            });
        }
    }


    /*
    private List<Point> processMeasurementMap(List<Measurement> measurements, Instant timestamp, String name) {
        List<Point> listOfPoints = new ArrayList<>();
        measurements.forEach( (m) -> {

            Point.Builder builder = Point.measurement(name)
                .time(timestamp.getEpochSecond(), TimeUnit.SECONDS)
                .tag(m.tags)
                .fields(m.fields);

            listOfPoints.add(builder.build());
        });

        return listOfPoints;
    }*/


    private List<Point> processMeasurementMap(List<Measurement> measurements, String name) {
        List<Point> listOfPoints = new ArrayList<>();
        measurements.forEach( (m) -> {
            log.trace("processMeasurementMap() - timestamp: {}, tags: {}, fields: {}", m.timestamp, m.tags, m.fields);
            Point.Builder builder = Point.measurement(name)
                .time(m.timestamp.getEpochSecond(), TimeUnit.SECONDS)
                .tag(m.tags)
                .fields(m.fields);
            listOfPoints.add(builder.build());
        });
        return listOfPoints;
    }

}
