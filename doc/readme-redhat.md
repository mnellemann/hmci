# Instruction for RedHat / CentOS / AlmaLinux Systems

Please note that the software versions referenced in this document might have changed and might not be available/working unless updated.

More details are available in the [README.md](../README.md) file. If you are running Linux on Power (ppc64le) you should look for ppc64le packages at the [Power DevOps](https://www.power-devops.com/) website.

All commands should be run as root or through sudo.

## Install the Java Runtime from repository

```shell
dnf install java-11-openjdk-headless
# or
yum install java-11-openjdk-headless
```


## Download and Install InfluxDB

```shell
wget https://dl.influxdata.com/influxdb/releases/influxdb-1.8.10.x86_64.rpm
rpm -ivh influxdb-1.8.10.x86_64.rpm
systemctl daemon-reload
systemctl enable influxdb
systemctl start influxdb
```

Run the ```influx``` cli command and create the *hmci* database.


## Download and Install Grafana

```shell
wget https://dl.grafana.com/oss/release/grafana-9.1.3-1.x86_64.rpm
rpm -ivh grafana-9.1.3-1.x86_64.rpm
systemctl daemon-reload
systemctl enable grafana-server
systemctl start grafana-server
```

When logged in to Grafana (port 3000, admin/admin) create a datasource that points to the local InfluxDB. Now import the provided dashboards.


## Download and Install HMCi

[Download](https://git.data.coop/nellemann/-/packages/generic/hmci/) the latest version of HMCi packaged for rpm.

```shell
rpm -ivh hmci-1.3.1-1_all.rpm
cp /opt/hmci/doc/hmci.toml /etc/
cp /opt/hmci/doc/hmci.service /etc/systemd/system/
systemctl daemon-reload
systemctl enable hmci
systemctl start hmci
```

Now modify */etc/hmci.toml* and test your setup by running ```/opt/hmci/bin/hmci -d``` manually and verify connection to HMC and InfluxDB. Afterwards start service with ```systemctl start hmci``` .
