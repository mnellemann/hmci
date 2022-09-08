# Instructions for AIX Systems

Please note that the software versions referenced in this document might have changed and might not be available/working unless updated.

More details are available in the [README.md](../README.md) file.

- Grafana and InfluxDB can be downloaded from the [Power DevOps](https://www.power-devops.com/) website - look under the *Monitor* section.

- Ensure Java (version 8 or later) is installed and available in your PATH.


## Download and Install HMCi

```shell
wget https://bitbucket.org/mnellemann/hmci/downloads/hmci-1.3.1-1_all.rpm
rpm -i --ignoreos hmci-1.3.1-1_all.rpm
cp /opt/hmci/doc/hmci.toml /etc/
```

Now modify */etc/hmci.toml* and test your setup by running ```/opt/hmci/bin/hmci -d```
