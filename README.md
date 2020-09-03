# HMC Insights

HMCi is a small utility to fetch metrics from one or more HMC's and push those to an InfluxDB time-series database.

![screenshot](https://bitbucket.org/mnellemann/hmci/downloads/HMCi.png)


## Usage Instructions

- Ensure you have correct date/time and NTP running to keep it accurate!
- Install HMCi *.deb* or *.rpm* file from [downloads](https://bitbucket.org/mnellemann/hmci/downloads/) or compile from source
- Copy the *doc/hmci.groovy.tpl* configuration template into */etc/hmci.groovy* and edit the configuration to suit your environment
- Configure Grafana to communicate with your InfluxDB and import dashboards from *doc/* into Grafana (The dashboards are slightly modified versions of the dashboard provided by the nmon2influxdb tool)
- Run the *bin/hmci* program in a shell, as a @reboot cron task or setup a proper service :)


### Power Binaries

You can download [Grafana](https://www.power-devops.com/grafana) and [InfluxDB](https://www.power-devops.com/influxdb) ppc64le packages for most Linux distributions and AIX from the [Power DevOps](https://www.power-devops.com/) site.

#### Notes

Examples on how to change the default InfluxDB retention policy:

     ALTER RETENTION POLICY "autogen" ON "hmci" DURATION 156w
     ALTER RETENTION POLICY "autogen" ON "hmci" DURATION 90d

#### InfluxDB and Grafana Packages

You can download [Grafana ppc64le](https://www.power-devops.com/grafana) and [InfluxDB ppc64le](https://www.power-devops.com/influxdb) packages for most Linux distributions and AIX on the [Power DevOps](https://www.power-devops.com/) site.

Binaries for amd64/x86 are available from the [Grafana website](https://grafana.com/grafana/download) and [InfluxDB website](https://portal.influxdata.com/downloads/) and also directly from your Linux distribution repository in some cases.

## Development Information

You need JDK version 8 or later.

### Build & Test

Use the gradle build tool, which will download all required dependencies.

    ./gradlew clean build


#### InfluxDB for local testing

Start the InfluxDB container

    docker run --name=influxdb --rm -d -p 8086:8086 influxdb

To use the Influx client from the same container

    docker exec -it influxdb influx


#### Grafana for local testing

Start the Grafana container, linking it to the InfluxDB container

    docker run --name grafana --link influxdb:influxdb --rm -d -p 3000:3000 grafana/grafana:7.1.3

Configure a new InfluxDB datasource on *http://influxdb:8086* named *hmci* to connect to the InfluxDB container. The database must be created beforehand, this can be done by running the hmci tool first. Grafana dashboards can be imported from the *doc/* folder.
