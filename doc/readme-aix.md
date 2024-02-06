# Instructions for AIX Systems

Ensure you have **correct date/time** and NTPd running to keep it accurate!

Please note that the software versions referenced in this document might have changed and might not be available/working unless updated.

- Grafana and InfluxDB can be downloaded from the [Power DevOps](https://www.power-devops.com/) website - look under the *Monitor* section.

- Ensure Java (version 8 or later) is installed and available in your PATH (eg. in the */etc/environment* file).


## Download and Install HMCi

[Download](https://github.com/mnellemann/hmci/) the latest version of HMCi package for rpm.

```shell
rpm -ivh --ignoreos hmci-1.4.2-1_all.rpm
cp /opt/hmci/doc/hmci.toml /etc/
```

Now modify */etc/hmci.toml* and test your setup by running ```/opt/hmci/bin/hmci -d```

