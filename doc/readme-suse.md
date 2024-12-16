# Instruction for SLES / OpenSUSE Systems

Please note that the software versions referenced in this document might have changed and might not be available/working unless updated.

Ensure you have **correct date/time** and NTPd running to keep it accurate!

All commands should be run as root or through sudo.

## Install the Java Runtime from repository

```shell
zypper install java-11-openjdk-headless wget
```


## Download and Install InfluxDB

```shell
wget https://dl.influxdata.com/influxdb/releases/influxdb-1.8.10.x86_64.rpm
rpm -ivh influxdb-1.8.10.x86_64.rpm
systemctl daemon-reload
systemctl enable influxdb
systemctl start influxdb
```

If you are running Linux on Power, you can find ppc64le InfluxDB packages on the [Power DevOps](https://www.power-devops.com/influxdb) site. Remember to pick the 1.8 or 1.9 version.

Run the ```influx``` cli command and create the *hmci* database.

```sql
CREATE DATABASE "hmci" WITH DURATION 365d REPLICATION 1;
```


## Download and Install Grafana

```shell
wget https://dl.grafana.com/oss/release/grafana-9.1.7-1.x86_64.rpm
rpm -ivh --nodeps grafana-9.1.7-1.x86_64.rpm
systemctl daemon-reload
systemctl enable grafana-server
systemctl start grafana-server
```

If you are running Linux on Power, you can find ppc64le Grafana packages on the [Power DevOps](https://www.power-devops.com/grafana) site.


## Download and Install HMCi

[Download](https://github.com/mnellemann/hmci/releases) the latest version of HMCi packaged for rpm.

```shell
rpm -ivh hmci-2.0.2-1_all.rpm
```

## Configure HMCi

Now modify **/etc/hmci.toml** (edit URL and credentials to your HMCs) and test the setup by running ```/opt/hmci/bin/hmci -d``` in the foreground/terminal and look for any errors.

Press CTRL+C to stop and then start as a background service with ```systemctl start hmci```.

You can see the log/output by running ```journalctl -f -u hmci```.


