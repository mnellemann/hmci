# HMCi as a System Service

## Systemd

To install as a systemd service, copy the **hmci.service**
file into */etc/systemd/system/* and enable the service:

    systemctl daemon-reload
    systemctl enable hmci.service
    systemctl restart hmci.service

To read log output from the service, use:

    journalctl -f -u hmci.service
