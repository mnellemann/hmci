# HMC Insights / HMCi

**HMCi** is a utility that collects metrics from one or more *IBM Power Hardware Management Consoles (HMC)*, without the need to install agents on logical partitions / virtual machines running on the IBM Power systems. The metric data is processed and saved into an InfluxDB time-series database. Grafana is used to visualize the metrics data from InfluxDB through provided dashboards, or your own customized dashboards.

This software is free to use and is licensed under the [Apache 2.0 License](LICENSE), but is not supported or endorsed by International Business Machines (IBM).

Metrics includes:

 - *Managed Systems* - the physical Power servers
 - *Logical Partitions* - the virtualized servers running AIX, Linux and/or IBM-i (AS/400)
 - *Virtual I/O Servers* - the i/o partition(s) virtualizing network and storage
 - *Energy* - watts and temperatures (needs to be enabled and is not available on all systems)

![architecture](doc/HMCi.png)

Some of my other related projects are:

- [svci](https://github.com/mnellemann/svci) for monitoring IBM Spectrum Virtualize (Flashsystems / Storwize / SVC)
- [sysmon](https://github.com/mnellemann/sysmon) for monitoring all types of servers with a small Java agent
- [syslogd](https://github.com/mnellemann/syslogd) for redirecting syslog and GELF to remote logging destinations

## Screenshots

![screenshot](doc/screenshots/sys-util.png)

Screenshots of other dashboards are available in the [screenshots](doc/screenshots/) folder.

## Installation and Setup

There are few steps in the installation.

1. Preparations of the Hardware Management Console (HMC)
2. Installation of InfluxDB, Grafana and the HMCi (this) software
3. Configure Grafana and import example dashboards


### 1 - Preparations of the Hardware Management Console (HMC)

On the HMC we create a *viewer* user which are used to fetch data through it's REST API and enable collection of performance metrics.

Read the [readme-hmc.md](doc/readme-hmc.md) file for instructions and help.


### 2 - Installation of InfluxDB, Grafana and the HMCi software

The required software can be installed together on the same LPAR / VM or separately, as long as it's possible to ensure network connectivity.

We are going to install HMCi (this software) to fetch and process metrics from our HMC, InfluxDB (v. 1.8 or 1.9) for storing the metrics data and lastly Grafana for visualizing the data.

Depending on your operating system, go through one of the documents:
 - [readme-aix.md](doc/readme-aix.md) for AIX.
 - [readme-suse.md](doc/readme-suse.md) for SUSE Enterprise Linux and OpenSUSE Linux.
 - [readme-redhat.md](doc/readme-redhat.md) for RedHat Enterprise Linux, AlmaLinux and RockyLinux.
 - [readme-debian.md](doc/readme-debian.md) for Debian GNU/Linux and derivatives such as Ubuntu.

Packages for Debian/Ubuntu & RedHat/SUSE are available from the [release](https://github.com/mnellemann/hmci/releases) page.


### 3. Configure Grafana and import example dashboards

Read the [readme-grafana.md](doc/readme-grafana.md) file for instructions and help.


## Notes

### No data (or past/future data) shown in Grafana

This is most likely due to timezone, date and/or NTP not being configured correctly on the HMC and/or host running HMCi.

You can check the timestamp of the most recent data by querying InfluxDB with the ```influx``` CLI client, and take note of the timezone when comparing:

```sql
use hmci;
precision rfc3339;
SELECT * FROM server_details GROUP BY * ORDER BY DESC LIMIT 1;
```

### Compatibility with nextract Plus

From version 1.2 *HMCi* is made compatible with the similar [nextract Plus](https://www.ibm.com/support/pages/nextract-plus-hmc-rest-api-performance-statistics) tool from  Nigel Griffiths. This means that the Grafana [dashboards](https://grafana.com/grafana/dashboards/13819) made by Nigel are compatible with *HMCi* and the other way around.


### InfluxDB Retention Policy

Examples for changing the default InfluxDB retention policy for the hmci database:

```text
ALTER RETENTION POLICY "autogen" ON "hmci" DURATION 156w
ALTER RETENTION POLICY "autogen" ON "hmci" DURATION 90d
```

### Upgrading HMCi

On RPM based systems (RedHat, Suse, CentOS), download the latest *hmci-x.y.z-n.noarch.rpm* file and upgrade:
```shell
rpm -Uvh hmci-x.y.z-n.noarch.rpm
```

On DEB based systems (Debian, Ubuntu and derivatives), download the latest *hmci_x.y.z-n_all.deb* file and upgrade:
```shell
dpkg -i hmci_x.y.z-n_all.deb
```

Restart the HMCi service on *systemd* based Linux systems:

```shell
systemctl restart hmci
journalctl -f -u hmci  # to check log output
```

On AIX you need to pass the *--ignoreos* flag to the *rpm* command:

```shell
rpm -Uvh --ignoreos hmci-x.y.z-n.noarch.rpm
```

## Possible problems

### Incomplete set of metrics

I have not been able to test and verify all types of metric data. If you encounter any missing or wrong data, please [contact me](mark.nellemann@gmail.com) and I will try to fix it.


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

#### InfluxDB v1.x

Start a InfluxDB container:

```shell
docker run --name=influxdb --rm -d -p 8086:8086 influxdb:1.8
```

Create the *hmci* database:

```shell
docker exec -i influxdb influx -execute "CREATE DATABASE hmci"
```

#### InfluxDB v2.x

Start a InfluxDB container:

```shell
docker pull influxdb:latest
docker run --name=influxdb --rm -d -p 8086:8086 influxdb:latest
```

- Then use the Web UI to create an initial user (for the web UI), an organization and bucket: http://localhost:8086/
- Then create an API token with RW access to your bucket.


#### Grafana

Start a Grafana container, linking it to the InfluxDB container:

```shell
docker run --name grafana --link influxdb:influxdb --rm -d -p 3000:3000 grafana/grafana
```

Setup Grafana to connect to the InfluxDB container by defining a new datasource on URL *http://influxdb:8086* named *hmci*.

If you are [connecting](https://docs.influxdata.com/influxdb/v2.7/tools/grafana/) to InfluxDB v2.x, then add a custom http header, enter bucket as database and disable authorization.
- Authorization = Token abcdef_random_token_from_nfluxdb==

Import dashboards from the [doc/dashboards/](doc/dashboards/) folder.
