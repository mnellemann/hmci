# Instruction for SLES / OpenSUSE Systems

Please note that the software versions referenced in this document might have changed and might not be available/working unless updated.

More details are available in the [README.md](../README.md) file. If you are running Linux on Power (ppc64le) you should look for ppc64le packages at the [Power DevOps](https://www.power-devops.com/) website.


## Install the Java Runtime from repository

```shell
sudo zypper install java-11-openjdk-headless
```


## Download and Install InfluxDB

```shell
wget https://dl.influxdata.com/influxdb/releases/influxdb-1.8.10.x86_64.rpm
sudo yum localinstall influxdb-1.8.10.x86_64.rpm
sudo systemctl daemon-reload
sudo systemctl enable influxdb
sudo systemctl start influxdb
```

Run the ```influx``` cli command and create the *hmci* database.


## Download and Install Grafana

```shell
wget https://dl.grafana.com/oss/release/grafana-9.1.3-1.x86_64.rpm
sudo rpm -i --nodeps grafana-9.1.3-1.x86_64.rpm
sudo systemctl daemon-reload
sudo systemctl enable grafana-server
sudo systemctl start grafana-server
```

When logged in to Grafana (port 3000, admin/admin) create a datasource that points to the local InfluxDB. Now import the provided dashboards.


## Download and Install HMCi

```shell
wget https://bitbucket.org/mnellemann/hmci/downloads/hmci_1.3.1-1_all.deb
sudo dpkg -i hmci_1.3.1-1_all.deb
cp /opt/hmci/doc/hmci.toml /etc/
cp /opt/hmci/doc/hmci.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable hmci
```

Now modify */etc/hmci.toml* and test your setup by running ```/opt/hmci/bin/hmci -d``` manually and verify connection to HMC and InfluxDB. Afterwards start service with ```systemctl start hmci``` .
