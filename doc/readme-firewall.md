# Firewall Notes

## RedHat, CentOS, Rocky & Alma Linux

And any other Linux distribution using *firewalld*.

### Allow remote access to Grafana on port 3000
```shell
firewall-cmd --zone=public --add-port=3000/tcp --permanent
firewall-cmd --reload
```
