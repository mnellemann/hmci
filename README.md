# HMC Insights

**HMCi** is a utility that collects metrics from one or more *IBM Power HMC* systems. The metric data is processed and saved into an InfluxDB time-series database. Grafana is used to visualize the metrics from InfluxDB.

Metrics includes *Managed Systems*  (the physical Power servers) and *Logical Partitions* (the virtualized servers) running AIX, Linux and IBM-i (AS/400).

![architecture](https://bitbucket.org/mnellemann/hmci/downloads/HMCi.png)

## Installation and Setup

### HMC Setup Instructions

- Login to your HMC
- Navigate to *Users and Security*
- Create a new read-only **hmci** user, which will be used to connect to the REST API.
- Click *Manage User Profiles and Access*, edit the newly created hmci user and click *User Properties*:
    - Enable *Allow remote access via the web*
    - Set *Session timeout in minutes* to **0**
- Navigate to *HMC Mangement* and *Console Settings*
- Click *Change Performance Monitoring Settings*:
    - Enable *Performance Monitoring Data Collection for Managed Servers*:  **All On**
    - Set *Performance Data Storage* to **1** day or more

### InfluxDB and Grafana Setup Instructions

Install InfluxDB on an *LPAR* or other server, which is network accessible by the *HMCi* utility (the default InfluxDB port is 8086). You can install Grafana on the same server or any server which are able to connect to the InfluxDB database. The Grafana installation needs to be accessible from your browser. The default settings for both InfluxDB and Grafana will work fine as a start.

- You can download [Grafana ppc64le](https://www.power-devops.com/grafana) and [InfluxDB ppc64le](https://www.power-devops.com/influxdb) packages for most Linux distributions and AIX on the [Power DevOps](https://www.power-devops.com/) site.
- Binaries for amd64/x86 are available from the [Grafana website](https://grafana.com/grafana/download) and [InfluxDB website](https://portal.influxdata.com/downloads/) and most likely directly from your Linux distributions repositories.

### HMCi Installation Instructions

- Ensure you have correct date/time and NTPd running to keep it accurate!
- The only requirement for **hmci** is the Java runtime, version 8 (or later)
- Install **HMCi** from [downloads](https://bitbucket.org/mnellemann/hmci/downloads/) (rpm, deb or jar) or build from source
- Copy the *doc/hmci.toml* configuration example into */etc/hmci.toml* and edit the configuration to suit your environment. The location of the configuration file can be changed with a flag when running hmci.
- Run the *bin/hmci* program in a shell, as a @reboot cron task or setup a proper service :)
- When started, *hmci* will try to create the InfluxDB database named hmci, if not found.
- Configure Grafana to communicate with your InfluxDB and import dashboards from the *doc/* folder into Grafana.


## Grafana Screenshots

Below are screenshots of the provided Grafana dashboards (found in the **doc/** folder), which can be used as a starting point.

- [hmci-resources.png](https://bitbucket.org/mnellemann/hmci/downloads/hmci-resources.png)
- [hmci-energy.png](https://bitbucket.org/mnellemann/hmci/downloads/hmci-energy.png)
- [hmci-vois.png](https://bitbucket.org/mnellemann/hmci/downloads/hmci-vios.png)
- [hmci-lpars](https://bitbucket.org/mnellemann/hmci/downloads/hmci-lpars.png)


## Notes

### InfluxDB

Per default the *hmci* influx database has no retention policy, so data will be kept forever. It is recommended to set a retention policy, which is shown below.

Examples for changing the default InfluxDB retention policy for the hmci database:

     ALTER RETENTION POLICY "autogen" ON "hmci" DURATION 156w
     ALTER RETENTION POLICY "autogen" ON "hmci" DURATION 90d


## Development Information

You need Java (JDK) version 8 or later to build hmci.


### Build & Test

Use the gradle build tool, which will download all required dependencies:

    ./gradlew clean build


### Local Testing

#### InfluxDB container

Start the InfluxDB container:

    docker run --name=influxdb --rm -d -p 8086:8086 influxdb

To execute the Influx client from within the container:

    docker exec -it influxdb influx


#### Grafana container

Start the Grafana container, linking it to the InfluxDB container:

    docker run --name grafana --link influxdb:influxdb --rm -d -p 3000:3000 grafana/grafana:7.1.3

Setup Grafana to connect to the InfluxDB container by defining a new datasource on URL *http://influxdb:8086* named *hmci*.

The hmci database must be created beforehand, which can be done by running the hmci tool first.

Grafana dashboards can be imported from the *doc/* folder.
