# HMC Insights

Small utility to fetch metrics from one or more HMC's and push those to an InfluxDB time-series database.


## Usage Instructions

- Ensure you have correct date/time and NTP running to keep it accurate.

Modify the */opt/hmci/conf/hmci.groovy* configuration file to suit your environment and run the program:

    /opt/hmci/bin/hmci

Configure Grafana to communicate with your InfluxDB and import dashboards from *doc/* into Grafana. The dashboards are slightly modified versions of the dashboard provided by the nmon2influxdb tool.


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
