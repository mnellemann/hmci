# HMC Insights

Small utility to fetch metrics from one or more HMC's and push those to an InfluxDB time-series database.

## Known Problems

- When running on Windows, the data is collected and written to InfluxDB, but in Grafana there is no data.


## Usage Instructions

### Create Configuration

Modify the **/opt/hmci/conf/hmci.groovy** configuration file to suit your environment.


### Run HMCi Tool

Requires Java 8+ runtime

    /opt/hmci/bin/hmci



## Development Information

### Build & Test

Use the gradle build tool

    ./gradlew clean build


### InfluxDB for local testing

Start the InfluxDB container

    docker run --name=influxdb --rm -d -p 8086:8086 influxdb

To use the Influx client from the same container

    docker exec -it influxdb influx


### Grafana for local testing

Start the Grafana container, linking it to the InfluxDB container

    docker run --name grafana --link influxdb:influxdb --rm -d -p 3000:3000 grafana/grafana:7.1.3

Configure a new InfluxDB datasource on **http://influxdb:8086** named **hmci** to connect to the InfluxDB container. The database must be created beforehand, this can be done by running the hmci tool first. Grafana dashboards can be imported from the **doc/** folder.
