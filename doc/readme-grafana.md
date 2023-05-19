# Grafana Setup

When installed Grafana listens on [http://localhost:3000](http://localhost:3000) and you can login as user *admin* with password *admin*. Once logged in you are asked to change the default password.

## Datasource

- Configure Grafana to use InfluxDB as a new datasource
  - Name the datasource **hmci** to make it obvious what it contains.
  - You would typically use *http://localhost:8086* without any credentials.
  - For InfluxDB 2.x add a custom header: Authorization = Token myTokenFromInfluxDB
  - The name of the database would be *hmci* (or another name you used when creating it)
  - **NOTE:** set *Min time interval* to *30s* or *1m* depending on your HMCi *refresh* setting.

## Dashboards

Import all or some of the example dashboards from [dashboards/*.json](dashboards/) into Grafana as a starting point and get creative making your own cool dashboards - please share anything useful :)

- When importing a dashboard, select the **hmci** datasource you have created.


## Security and Proxy

The easiest way to secure Grafana with https is to put it behind a proxy server such as nginx.

If you want to serve /grafana as shown below, you also need to edit */etc/grafana/grafana.ini* and change the *root_url*:

```
root_url = %(protocol)s://%(domain)s:%(http_port)s/grafana/
```

Nginx snippet:

```nginx
    location /grafana/ {
        proxy_pass      http://localhost:3000/;
        proxy_set_header Host $host;
    }
```


