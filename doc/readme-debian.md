# Instruction for Debian / Ubuntu Systems

Please note that the software versions referenced in this document might have changed and might not be available/working unless updated.

Ensure you have **correct date/time** and NTPd running to keep it accurate!

All commands should be run as root or through sudo.

## Install the Java Runtime from repository

```shell
apt-get install default-jre-headless wget
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

```sql
CREATE DATABASE "hmci" WITH DURATION 365d REPLICATION 1;
```

## Download and Install Grafana

```shell
apt-get install -y adduser libfontconfig1
wget https://dl.grafana.com/oss/release/grafana_9.1.7_amd64.deb
dpkg -i grafana_9.1.7_amd64.deb
systemctl daemon-reload
systemctl enable grafana-server
systemctl start grafana-server
```



## Download and Install HMCi

[Download](https://git.data.coop/nellemann/-/packages/generic/hmci/) the latest version of HMCi packaged for deb.

```shell
wget https://git.data.coop/api/packages/nellemann/generic/hmci/v1.4.2/hmci_1.4.2-1_all.deb
dpkg -i hmci_1.4.2-1_all.deb
cp /opt/hmci/doc/hmci.toml /etc/
cp /opt/hmci/doc/hmci.service /etc/systemd/system/
systemctl daemon-reload
systemctl enable hmci
```

## Configure HMCi

Now modify **/etc/hmci.toml** (edit URL and credentials to your HMCs) and test the setup by running ```/opt/hmci/bin/hmci -d``` in the foreground/terminal and look for any errors.

Press CTRL+C to stop and then start as a background service with ```systemctl start hmci```.

You can see the log/output by running ```journalctl -f -u hmci```.
