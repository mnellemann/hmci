# Instruction for Debian / Ubuntu Systems

Please note that the software versions referenced in this document might have changed and might not be available/working unless updated.

More details are available in the [README.md](../README.md) file.

All commands should be run as root or through sudo.

## Install the Java Runtime from repository

```shell
apt-get install default-jre-headless
```


## Download and Install InfluxDB

```shell
wget https://dl.influxdata.com/influxdb/releases/influxdb_1.8.10_amd64.deb
dpkg -i influxdb_1.8.10_amd64.deb
systemctl daemon-reload
systemctl enable influxdb
systemctl start influxdb
```

Run the ```influx``` cli command and create the *hmci* database.


## Download and Install Grafana

```shell
sudo apt-get install -y adduser libfontconfig1
wget https://dl.grafana.com/oss/release/grafana_9.1.3_amd64.deb
dpkg -i grafana_9.1.3_amd64.deb
systemctl daemon-reload
systemctl enable grafana-server
systemctl start grafana-server
```

When logged in to Grafana (port 3000, admin/admin) create a datasource that points to the local InfluxDB. Now import the provided dashboards.


## Download and Install HMCi

```shell
wget https://bitbucket.org/mnellemann/hmci/downloads/hmci_1.3.1-1_all.deb
dpkg -i hmci_1.3.1-1_all.deb
cp /opt/hmci/doc/hmci.toml /etc/
cp /opt/hmci/doc/hmci.service /etc/systemd/system/
systemctl daemon-reload
systemctl enable hmci
```

Now modify */etc/hmci.toml* and test setup by running ```/opt/hmci/bin/hmci -d``` manually and verify connection to HMC and InfluxDB. Afterwards start service with ```systemctl start hmci``` .
