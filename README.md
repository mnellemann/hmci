# HMC Insights

**HMCi** is a utility that collects metrics from one or more *IBM Power HMC*. The metric data is processed and saved into an InfluxDB time-series database. Grafana can be used to visualize the metrics from InfluxDB. This software is free to use and is licensed under the [Apache 2.0 License](https://bitbucket.org/mnellemann/syslogd/src/master/LICENSE), but is not supported or endorsed by International Business Machines (IBM).

Metrics includes:

 - *Managed Systems* - the physical Power servers
 - *Logical Partitions* - the virtualized servers running AIX, Linux and IBM-i (AS/400)
 - *Virtual I/O Servers* - the i/o partition(s) taking care of network and storage
 - *Energy* - power consumption and temperatures (needs to be enabled and not available for E880, E980)

![architecture](https://bitbucket.org/mnellemann/hmci/downloads/HMCi.png)

## Installation and Setup

### Power HMC Setup Instructions

- Login to your HMC
- Navigate to *Users and Security*
- Create a new read-only **hmci** user, which will be used to connect to the REST API.
- Click *Manage User Profiles and Access*, edit the newly created hmci user and click *User Properties*:
    - Enable *Allow remote access via the web*
    - Set *Session timeout minutes* to **120**
    - Set *Verify timeout minutes* to **15**
    - Set *Idle timeout minutes* to **15**
    - Set *Minimum time in days between password changes* to **0**
- Navigate to *HMC Management* and *Console Settings*
- Click *Change Performance Monitoring Settings*:
    - Enable *Performance Monitoring Data Collection for Managed Servers*:  **All On**
    - Set *Performance Data Storage* to **1** day or preferable more

### InfluxDB and Grafana Setup Instructions

Install InfluxDB on an *LPAR* or other server, which is network accessible by the *HMCi* utility (the default InfluxDB port is 8086). You can install Grafana on the same server or any server which are able to connect to the InfluxDB database. The Grafana installation needs to be accessible from your browser. The default settings for both InfluxDB and Grafana will work fine as a start.

- You can download [Grafana ppc64le](https://www.power-devops.com/grafana) and [InfluxDB ppc64le](https://www.power-devops.com/influxdb) packages for most Linux distributions and AIX on the [Power DevOps](https://www.power-devops.com/) site.
- Binaries for amd64/x86 are available from the [Grafana website](https://grafana.com/grafana/download) and [InfluxDB website](https://portal.influxdata.com/downloads/) and most likely directly from your Linux distributions repositories.

### HMCi Installation Instructions

- Ensure you have **correct date/time** and NTPd running to keep it accurate!
- The only requirement for **hmci** is the Java runtime, version 8 (or later)
- Install **HMCi** from [downloads](https://bitbucket.org/mnellemann/hmci/downloads/) (rpm, deb or jar) or build from source
- Copy the *doc/hmci.toml* configuration example into */etc/hmci.toml* and edit the configuration to suit your environment. The location of the configuration file can be changed with the *--conf* option.
- Run the *bin/hmci* program in a shell, as a @reboot cron task or setup a proper service :) There is a systemd service example in the *doc/* folder.
- When started, *hmci* will try to create the InfluxDB database named hmci, if not found.
- Configure Grafana to communicate with your InfluxDB and import dashboards from the *doc/* folder into Grafana.

## Notes

### Compatibility with nextract Plus

From version 1.2 *HMCi* is made compatible with the similar [nextract Plus](https://www.ibm.com/support/pages/nextract-plus-hmc-rest-api-performance-statistics) tool from  Nigel Griffiths. This means you can use the excellent Grafana [dashboards](https://grafana.com/grafana/dashboards/13819) made by Nigel with *HMCi*.

### Start InfluxDB and Grafana at boot on RedHat 7+

```shell
systemctl enable influxdb
systemctl start influxdb

systemctl enable grafana-server
systemctl start grafana-server
```

### InfluxDB Retention Policy

Per default the *hmci* influx database has no retention policy, so data will be kept forever. It is recommended to set a retention policy, which is shown below.

Examples for changing the default InfluxDB retention policy for the hmci database:

```text
ALTER RETENTION POLICY "autogen" ON "hmci" DURATION 156w
ALTER RETENTION POLICY "autogen" ON "hmci" DURATION 90d
```

## Grafana Screenshots

Below are screenshots of the provided Grafana dashboards (found in the **doc/** folder), which can be used as a starting point.

 - [hmci-resources.png](https://bitbucket.org/mnellemann/hmci/downloads/hmci-resources.png)
 - [hmci-energy.png](https://bitbucket.org/mnellemann/hmci/downloads/hmci-energy.png)
 - [hmci-vois.png](https://bitbucket.org/mnellemann/hmci/downloads/hmci-vios.png)
 - [hmci-lpars](https://bitbucket.org/mnellemann/hmci/downloads/hmci-lpars.png)

## Known problems

### Naming collision

You can't have partitions (or Virtual I/O Servers) on different Systems with the same name, as these cannot be distinguished when metrics are
written to InfluxDB (which uses the name as key).

### Renaming partitions

If you rename a partition, the metrics in InfluxDB will still be available by the old name, and new metrics will be available by the new name of the partition. There is no easy way to migrate the old data, but you can delete it easily:

```text
DELETE WHERE partition = 'lpar-name';
```

## Development Information

You need Java (JDK) version 8 or later to build hmci.


### Build & Test

Use the gradle build tool, which will download all required dependencies:

```shell
./gradlew clean build
```

### Local Testing

#### InfluxDB container

Start the InfluxDB container:

```shell
docker run --name=influxdb --rm -d -p 8086:8086 influxdb:1.8-alpine
```

To execute the Influx client from within the container:

```shell
docker exec -it influxdb influx
```

#### Grafana container

Start the Grafana container, linking it to the InfluxDB container:

```shell
docker run --name grafana --link influxdb:influxdb --rm -d -p 3000:3000 grafana/grafana:7.1.3
```

Setup Grafana to connect to the InfluxDB container by defining a new datasource on URL *http://influxdb:8086* named *hmci*.

The hmci database must be created beforehand, which can be done by running the hmci tool first.

Grafana dashboards can be imported from the *doc/* folder.
