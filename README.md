# HMC Insights

**HMCi** is a utility that collects metrics from one or more *IBM Power Hardware Management Consoles (HMC)*, without the need to install agents on logical partitions / virtual machines running on the IBM Power systems. The metric data is processed and saved into an InfluxDB time-series database. Grafana is used to visualize the metrics from InfluxDB through provided dashboards, or your own customized dashboards.

This software is free to use and is licensed under the [Apache 2.0 License](https://bitbucket.org/mnellemann/syslogd/src/master/LICENSE), but is not supported or endorsed by International Business Machines (IBM). There is an optional [companion agent](https://bitbucket.org/mnellemann/sysmon/), which provides more metrics from within AIX and Linux.

Metrics includes:

 - *Managed Systems* - the physical Power servers
 - *Logical Partitions* - the virtualized servers running AIX, Linux and IBM-i (AS/400)
 - *Virtual I/O Servers* - the i/o partition(s) virtualizing network and storage
 - *Energy* - power consumption and temperatures (needs to be enabled and is not available on P7 and multi-chassis systems)

![architecture](doc/HMCi.png)

## Installation and Setup

There are few steps in the installation.

1. Preparations on the Hardware Management Console (HMC)
2. Installation of InfluxDB and Grafana software
3. Installation and configuration of *HMC Insights* (HMCi)
4. Configure Grafana and import example dashboards

### 1 - IBM Power HMC Setup Instructions

- Login to your HMC
- Navigate to *Console Settings*
  - Go to *Change Date and Time*
    - Set correct timezone, if not done already
    - Configure one or more NTP servers, if not done already
    - Enable the NTP client, if not done already
- Navigate to *Users and Security*
  - Create a new read-only **hmci** user, which will be used to connect to the REST API.
  - Click *Manage User Profiles and Access*, edit the newly created *hmci* user and click *User Properties*:
    - **Enable** *Allow remote access via the web*
    - Set *Minimum time in days between password changes* to **0**
- Navigate to *HMC Management* and *Console Settings*
  - Click *Change Performance Monitoring Settings*:
    - Enable *Performance Monitoring Data Collection for Managed Servers*:  **All On**
    - Set *Performance Data Storage* to **1** day or preferable more

If you do not enable *Performance Monitoring Data Collection for Managed Servers*, you will see errors such as *Unexpected response: 403*. Use the HMCi debug option to get more details about what is going on.

### 2 - InfluxDB and Grafana Installation

Install InfluxDB (v. **1.8.x** or **1.9.x** for best compatibility with Grafana) on a host which is network accessible by the HMCi utility (the default InfluxDB port is 8086). You can install Grafana on the same server or any server which are able to connect to the InfluxDB database. The Grafana installation needs to be accessible from your browser (default on port 3000). The default settings for both InfluxDB and Grafana will work fine as a start.

- You can download [Grafana ppc64le](https://www.power-devops.com/grafana) and [InfluxDB ppc64le](https://www.power-devops.com/influxdb) packages for most Linux distributions and AIX on the [Power DevOps](https://www.power-devops.com/) site.
- Binaries for amd64/x86 are available from the [Grafana website](https://grafana.com/grafana/download) and [InfluxDB website](https://portal.influxdata.com/downloads/) and most likely directly from your Linux distributions repositories.
- Create the empty *hmci* database by running the **influx** cli command and type:

```text
CREATE DATABASE "hmci" WITH DURATION 365d REPLICATION 1;
```

See the [Influx documentation](https://docs.influxdata.com/influxdb/v1.8/query_language/manage-database/#create-database) for more information on duration and replication.

### 3 - HMCi Installation & Configuration

Install *HMCi* on a host, which can connect to the Power HMC (on port 12443), and is also allowed to connect to the InfluxDB service. This *can be* the same LPAR/VM as used for the InfluxDB installation.

- Ensure you have **correct date/time** and NTPd running to keep it accurate!
- The only requirement for **hmci** is the Java runtime, version 8 (or later)
- Install **HMCi** from [downloads](https://bitbucket.org/mnellemann/hmci/downloads/) (rpm, deb or jar) or build from source
  - On RPM based systems: **sudo rpm -i hmci-x.y.z-n.noarch.rpm**
  - On DEB based systems: **sudo dpkg -i hmci_x.y.z-n_all.deb**
- Copy the **/opt/hmci/doc/hmci.toml** configuration example into **/etc/hmci.toml** and edit the configuration to suit your environment. The location of the configuration file can be changed with the *--conf* option.
- Run the **/opt/hmci/bin/hmci** program in a shell, as a @reboot cron task or configure as a proper service - there are instructions in the *doc/readme-service.md* file.
- When started, *hmci* expects the InfluxDB database to be created by you.

### 4 - Grafana Configuration

- Configure Grafana to use InfluxDB as a new datasource
  - **NOTE:** set *Min time interval* to *30s* or *1m* depending on your HMCi *refresh* setting.
- Import example dashboards from [doc/*.json](doc/) into Grafana as a starting point and get creative making your own cool dashboards :)

## Notes

### No data (or past/future data) shown in Grafana

This is most likely due to timezone, date and/or NTP not being configured correctly on the HMC and/or host running HMCi.

Example showing how you configure related settings through the HMC CLI:
```shell
chhmc -c date -s modify --datetime MMDDhhmm           # Set current date/time: MMDDhhmm[[CC]YY][.ss]
chhmc -c date -s modify --timezone Europe/Copenhagen  # Configure your timezone
chhmc -c xntp -s enable                               # Enable the NTP service
chhmc -c xntp -s add -a IP_Addr                       # Add a remote NTP server
```
Remember to reboot your HMC after changing the timezone.

### Compatibility with nextract Plus

From version 1.2 *HMCi* is made compatible with the similar [nextract Plus](https://www.ibm.com/support/pages/nextract-plus-hmc-rest-api-performance-statistics) tool from  Nigel Griffiths. This means that the Grafana [dashboards](https://grafana.com/grafana/dashboards/13819) made by Nigel are compatible with *HMCi* and the other way around.

### Start InfluxDB and Grafana at boot (systemd compatible Linux)

```shell
systemctl enable influxdb
systemctl start influxdb

systemctl enable grafana-server
systemctl start grafana-server
```

### InfluxDB Retention Policy

Examples for changing the default InfluxDB retention policy for the hmci database:

```text
ALTER RETENTION POLICY "autogen" ON "hmci" DURATION 156w
ALTER RETENTION POLICY "autogen" ON "hmci" DURATION 90d
```

### Upgrading HMCi

On RPM based systems (RedHat, Suse, CentOS), download the latest *hmci-x.y.z-n.noarch.rpm* file and upgrade:
```shell
sudo rpm -Uvh hmci-x.y.z-n.noarch.rpm
```

On DEB based systems (Debian, Ubuntu and derivatives), download the latest *hmci_x.y.z-n_all.deb* file and upgrade:
```shell
sudo dpkg -i hmci_x.y.z-n_all.deb
```

Restart the HMCi service on *systemd* based Linux systems:

```shell
systemctl restart hmci
journalctl -f -u hmci  # to check log output
```


### AIX Notes

To install (or upgrade) on AIX, you need to pass the *--ignoreos* flag to the *rpm* command:

```shell
rpm -Uvh --ignoreos hmci-x.y.z-n.noarch.rpm
```


## Grafana Screenshots

Below are screenshots of the provided Grafana dashboards (found in the **doc/** folder), which can be used as a starting point.

 - [hmci-systems.png](https://bitbucket.org/mnellemann/hmci/downloads/hmci-systems-dashboard.png)
 - [hmci-vois.png](https://bitbucket.org/mnellemann/hmci/downloads/hmci-vios-dashboard.png)
 - [hmci-lpars](https://bitbucket.org/mnellemann/hmci/downloads/hmci-lpars-dashboard.png)


## Known problems

### Incomplete test of metrics

I have not been able to test and verify all types of metric data. If you encounter any missing or wrong data, please contact me, so I can try to fix it. It is possible to run **hmci** with *-d -d* to log JSON data received by the HCM, which can help me implement missing data.


### Naming collision

You can't have partitions (or Virtual I/O Servers) on different Systems with the same name, as these cannot be distinguished when metrics are
written to InfluxDB (which uses the name as key).

### Renaming partitions

If you rename a partition, the metrics in InfluxDB will still be available by the old name, and new metrics will be available by the new name of the partition. There is no easy way to migrate the old data, but you can delete it easily:

```text
DELETE WHERE lparname = 'name';
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
