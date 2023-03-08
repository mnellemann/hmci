# Grafana Setup

## Datasource

- Configure Grafana to use InfluxDB as a new datasource
  - Name the datasource **hmci** to make it obvious what it contains.
  - You would typically use *http://localhost:8086* without any credentials.
  - The name of the database would be *hmci* (or another name you used when creating it)
  - **NOTE:** set *Min time interval* to *30s* or *1m* depending on your HMCi *refresh* setting.

## Dashboards

Import all or some of the example dashboards from [dashboards/*.json](dashboards/) into Grafana as a starting point and get creative making your own cool dashboards - please share anything useful :)

- When importing a dashboard, select the **hmci** datasource you have created.
