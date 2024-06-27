# Running the HMCi as Container Podman POD stack 

Start HMCi, InfluxDB and Grafana as container with podman pod:


## Create a pod with network between the containers and export ports for grafana and influxdb.
> :bulb: Traffic out from container is normally allowed.

```shell
podman pod create -n hmci_metric_stack -p 8086:8086 -p 3000:3000
podman run --name influxdb -d -p 8086:8086 --pod hmci_metric_stack influxdb:latest
podman run --name grafana -d --pod hmci_metric_stack grafana/grafana
```

## Starting the HMCi - Metric Container.
Create a folder that contains the svi.toml
Crate the hmci.toml file with with your credentials/token for influxdb and HMC, se example below.

```shell
# Run interactive to check that it connects. 
podman run -i --name hmci_exporter --pod hmci_metric_stack  --volume ${PWD}/data:/opt/app/config/ ghcr.io/mnellemann/hmci:main
```
See that you get output `"[main] InfluxClient"`

## Run container in background
```shell
# Run in background
podman run --name hmci_exporter -d --pod hmci_metric_stack  --volume ${PWD}/data:/opt/app/config/ ghcr.io/mnellemann/hmci:main
```


<details closed>
  <summary>:bulb: Run HMCi container with options</summary>
    
    podman run --name hmci_exporter -pod hmci_metric_stack --volume ${PWD}/data:/opt/app/config/ ghcr.io/mnellemann/hmci:main java -jar /opt/app/hmci.jar/hmci-latest.jar -c /opt/app/config/hmci.toml -d
    
</details>



## Check status of the HMCi_Metric POD:

Check that all containers is running with `podman ps --pods`

```shell
podman ps --pod                        

CONTAINER ID  IMAGE                                                  COMMAND               CREATED       STATUS             PORTS                                           NAMES                POD ID        PODNAME
2c78533009c1  localhost/podman-pause:5.0.0-dev-8a643c243-1710720000                        2 months ago  Up About a minute  0.0.0.0:3000->3000/tcp, 0.0.0.0:8086->8086/tcp  19b5b49164c9-infra   19b5b49164c9  hmci_metric_stack
2e84bf1ee381  docker.io/library/influxdb:latest                      influxd               2 months ago  Up About a minute  0.0.0.0:3000->3000/tcp, 0.0.0.0:8086->8086/tcp  influxdb             19b5b49164c9  hmci_metric_stack
cd8844645b21  docker.io/grafana/grafana:latest                                             2 months ago  Up About a minute  0.0.0.0:3000->3000/tcp, 0.0.0.0:8086->8086/tcp  grafana              19b5b49164c9  hmci_metric_stack
06cdd8cbb9b8  ghcr.io/mnellemann/hmci:main                           java -jar /opt/ap...  28 hours ago  Up About a minute  0.0.0.0:3000->3000/tcp, 0.0.0.0:8086->8086/tcp  hmci_exporter  19b5b49164c9  hmci_metric_stack
```

To get logs from the hmci_exporter you can use podman logs
```shell
podman logs -f hmci_exporter
```

To log into the container use: exec -it with /bin/sh
```shell
podman exec -it hmci_exporter /bin/sh
```

## Create the config file for HMCi Metric Container.

We are providing a config file to supply the HMCi Metric with an IP and a user. This will enable it to connect to both the HMC and InfluxDB.

Create a Main folder to store the config file.
in the example we are using mainfolder hmci-metric and data. 
place the config file, hmci.toml inside data folder.

```shell
% pwd                     
-/hmci-metric
% ls
data
% ls         
hmci.toml
```
> :bulb: You could also created a container volume and add the config file.


## Creating a InfluxDB Bucket.
1. Open InfluxDB webpage: 
2. Click Data Explore > Click Create Bucket > name it hmci

## Creating a InfluxDB admin token.
1. Open InfluxDB webpage: 
2. Click Load Data > Custom API Token > name it something. 
3. Under Resource and Buckets, Chose your bucket. with read Write.
4. Pres generate and write down the token


# Grafana Setup

When installed Grafana listens on [http://localhost:3000](http://localhost:3000) and you can login as user *admin* with password *admin*. Once logged in you are asked to change the default password.

## Creating the influxdb source. 

When creating the influxdb source in grafana, we need the influxdb token you created earlier: default is uses this one:
`Token hTHG-mwhRypjO8nZEmdzVKL4fM7kJH7989MC9JdgXacVHfBsks8AzeIwhqv-sXm76dphjO5pvqv5Fmsvw_zvGA==`

Go to Grafana webpage: 
1. Click Add data source.
2. Select InfluxDB from the list of available data sources.
3. On the Data Source configuration page, enter a name for your InfluxDB data source. DS_HMCI
4. Under Query Language, InfluxQL
5. Configure Grafana to use 
  - a. Under HTTP, enter the following:
  URL: Your InfluxDB URL.(http://influxdb:8086) )(for podman pod it will use internal dns)
  - b. Under InfluxDB Details, enter the following:
  Default bucket : your InfluxDB bucket (hmci)
  HTTP Method: Select GET
  - c. Provide a Min time interval (default is 10s).  
    - **NOTE:** set *Min time interval* to *30s* or *1m* depending on your HMCi *refresh*
  - d. Create Custom HTTP Headers where Header=Authorization and Value=Token <your token> [ make sure that you are adding space between Token word and your token of InfluxDB]
  like: `Token hTHG-mwhRypjO8nZEmdzVKL4fM7kJH7989MC9JdgXacVHfBsks8AzeIwhqv-sXm76dphjO5pvqv5Fmsvw_zvGA==`
6. Press Save and test, should say sucess..


## Importing Grafana Dashboards. 

Import all or some of the example dashboards from [dashboards/*.json](dashboards/) into Grafana as a starting point and get creative making your own cool dashboards - please share anything useful :)

1. Go to Grafana webpage: 
2. Click Home and New > New Dashboard
3. Go to the HMCi github repo > doc folder > dashboards.
4. 2 Options
    1. Option 1.copy the json content, one dashboard at the time.
    2. Drag and drop the dashboard json file
5. Ensure that the Database is set to your database/source, example DS_HMCI and press import. 

> :bulb: If there is no data, please wait 5 min, then check that your timezone and date/time  is correct/same in both the Container. Default it's UTC. 
    See example here for how to set timezone in container. https://gist.github.com/sjimenez44/1b73afeae3eec26a1915b0d4d5873b8f

## Example Config HMCi Metric Container

Example for influxDB and one storage system.

> :bulb: To monitor more then one storage system just duplicated the [svc.xx] part. 

```shell
# HMCi Configuration
# Copy this file into /etc/hmci.toml and customize it to your environment.

###
### Define one InfluxDB to save metrics into
### There must be only one and it should be named [influx]
###

# InfluxDB v2.x example
[influx]
url = "http://influxdb:8086"
org = "test"
token = "hTHG-mwhRypjO8nZEmdzVKL4fM7kJH7989MC9JdgXacVHfBsks8AzeIwhqv-sXm76dphjO5pvqv5Fmsvw_zvGA=="
bucket = "hmci"


###
### Define one or more HMC's to query for metrics
### Each entry must be named [hmc.<something-unique>]
###


# HMC to query for data and metrics
[hmc.site1]
url = "https://10.33.3.71:12443"
username = "hmci"
password = "hmci1234!"
refresh = 30   # How often to query HMC for data - in seconds
discover = 120 # Rescan HMC for new systems and partitions - in minutes
trust = true   # Ignore SSL cert. errors (due to default self-signed cert. on HMC)
energy = true  # Collect energy metrics on supported systems


# Another HMC example
#[hmc.site2]
#url = "https://10.10.20.5:12443"
#username = "user"
#password = "password"
#trace = "/tmp/hmci-trace"                   # When present, store JSON metrics files from HMC into this folder
#excludeSystems = [ 'notThisSystem' ]        # Collect metrics from all systems except those listed here
#includeSystems = [ 'onlyThisSystems' ]      # Collcet metrics from no systems but those listed here
#excludePartitions = [ 'skipThisPartition' ] # Collect metrics from all partitions except those listed here
#includePartitions = [ 'onlyThisPartition' ] # Collect metrics from no partitions but those listed here

```

