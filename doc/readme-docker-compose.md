# Running the HMCi as Container in Compose 

Start HMCi, InfluxDB and Grafana as container with Podman/Docker Compose.
This will Create 3 containers, HMCi, Influxdb, and grafana. deployment will also create influx api key, and create a "bucket" for our data. (retention 365days) and same for Grafana, create the datasource against influxdb and import the grafana dashboards.  (PS, the dashboards are using the DS_HMCi as the datasource)


Full example is located under [examples/docker-podman-compose](examples/docker-podman-compose) folder.
To spinn up the stack you only need to change the IP,username and password for you storage device in the HMCi.toml file. located under [HMCi-data](examples/docker-podman-compose/hmci-data/hmci.toml)

1. Cd into the folder examples/docker-podman-compose
2. `docker-compose up -d` or `podman-compose up -d`  this will start up the system in background.
3. check status `podman-compose ps` 

    ```yaml
      using podman version: 4.4.1
      podman ps -a --filter label=io.podman.compose.project=metric-exporter-compose
      CONTAINER ID  IMAGE                              COMMAND               CREATED         STATUS         PORTS                                           NAMES
      4cb391753d12  docker.io/library/influxdb:latest  influxd               12 minutes ago  Up 12 minutes  0.0.0.0:8086->8086/tcp, 0.0.0.0:8088->8088/tcp  influxdb-hmci
      4a078e9b6827  docker.io/grafana/grafana:latest                         12 minutes ago  Up 12 minutes  0.0.0.0:3000->3000/tcp                          grafana-hmci
      92ee4496b728  ghcr.io/mnellemann/hmci:latest     java -jar /opt/ap...  12 minutes ago  Up 12 minutes                                                  storage_metric_exporter-hmci
      exit code: 0
    ```
    PS: this will create a default network with a bridge for access out. 

    ```shell
    # podman network ls
    NETWORK ID    NAME                                 DRIVER
    2f259bab93aa  podman                               bridge
    41d9b210dfbf  metric-exporter-compose_default  bridge
    ```

4. Using the default ports for grafana and influxdb, use the http://ip:3000 for grafana and http://ip:8086 for influxdb. password is locate in the .env file. 
5. If you want to shutdown the pod and remove the data use `podman-compose down --volumes`

-------

Example of the podman compose file. 
```yaml
services:
  influxdb:
    image: influxdb:latest
    container_name: influxdb-hmci
    ports:
      - 8086:8086
      - 8088:8088
    volumes:
      - influxdb-storage:/var/lib/influxdb
    environment:
      # .env files automatically picked up if exist in same folder.
      # PS: Password need to  be some length.  
      # if you dont want to have secrets in env, use podman/docker secret create
      # With the DOCKER_INFLUXDB_ Prefix this will then be picked up by influxdb container
      - DOCKER_INFLUXDB_INIT_MODE=${INFLUXDB_INIT_MODE}
      - DOCKER_INFLUXDB_INIT_USERNAME=${INFLUXDB_INIT_USERNAME} 
      - DOCKER_INFLUXDB_INIT_PASSWORD=${INFLUXDB_INIT_PASSWORD} 
      - DOCKER_INFLUXDB_INIT_BUCKET=${INFLUXDB_INIT_BUCKET}
      - DOCKER_INFLUXDB_INIT_ORG=${INFLUXDB_INIT_ORG}
      - DOCKER_INFLUXDB_INIT_RETENTION=${INFLUXDB_INIT_RETENTION}
      - DOCKER_INFLUXDB_INIT_ADMIN_TOKEN=${INFLUXDB_INIT_ADMIN_TOKEN}
    #networks:
    #  - external_network
  grafana:
    image: grafana/grafana:latest
    container_name: grafana-hmci
    ports:
      - 3000:3000
    volumes:
      - grafana-storage:/var/lib/grafana
      - ./grafana-provisioning/:/etc/grafana/provisioning/
    depends_on:
      - influxdb-hmci
    environment:
      - GF_SECURITY_ADMIN_USER=${GRAFANA_USERNAME}
      - GF_SECURITY_ADMIN_PASSWORD=${GRAFANA_PASSWORD}
      - DOCKER_INFLUXDB_INIT_MODE=${INFLUXDB_INIT_MODE}
      - DOCKER_INFLUXDB_INIT_USERNAME=${INFLUXDB_INIT_USERNAME} 
      - DOCKER_INFLUXDB_INIT_PASSWORD=${INFLUXDB_INIT_PASSWORD} 
      - DOCKER_INFLUXDB_INIT_BUCKET=${INFLUXDB_INIT_BUCKET}
      - DOCKER_INFLUXDB_INIT_ORG=${INFLUXDB_INIT_ORG}
      - DOCKER_INFLUXDB_INIT_RETENTION=${INFLUXDB_INIT_RETENTION}
      - DOCKER_INFLUXDB_INIT_ADMIN_TOKEN=${INFLUXDB_INIT_ADMIN_TOKEN}
    #networks:
    #  - external_network
  hmci:
    image: ghcr.io/mnellemann/hmci:latest
    container_name: storage_metric_exporter-hmci
    restart: unless-stopped
    #command:
    volumes:
      - ./hmci-data:/opt/app/config/
    depends_on:
      - grafana-hmci
    #networks:
    #  - external_network
#networks:
#  external_network:
#    external: true
volumes:
  influxdb-storage:
  grafana-storage:
```


Using .env file to get config into the containers. 
.env file

```
# Change to your's environment.
GRAFANA_USERNAME=admin
GRAFANA_PASSWORD=admin1234!
INFLUXDB_INIT_MODE=setup
INFLUXDB_INIT_USERNAME=admin
INFLUXDB_INIT_PASSWORD=admin1234!
INFLUXDB_INIT_ORG=test
INFLUXDB_INIT_BUCKET=hmci
INFLUXDB_INIT_RETENTION=356d
# Remeber to use same token in hmci.toml
INFLUXDB_INIT_ADMIN_TOKEN="hTHG-mwhRypjO8nZEmdzVKL4fM7kJH7989MC9JdgXacVHfBsks8AzeIwhqv-sXm76dphjO5pvqv5Fmsvw_zvGA=="
```



## Manually Creating the influxdb source in Grafana. 

When creating the influxdb source in grafana, we need the influxdb token you created earlier: default is uses this one:
`Token hTHG-mwhRypjO8nZEmdzVKL4fM7kJH7989MC9JdgXacVHfBsks8AzeIwhqv-sXm76dphjO5pvqv5Fmsvw_zvGA==`

Go to Grafana webpage: 
1. Click Add data source.
2. Select InfluxDB from the list of available data sources.
3. On the Data Source configuration page, enter a name for your InfluxDB data source.
4. Under Query Language, InfluxQL
5. Configure Grafana to use 
  - a. Under HTTP, enter the following:
  URL: Your InfluxDB URL.(http://influxdb:8086)
  - b. Under InfluxDB Details, enter the following:
  Default bucket : your InfluxDB bucket (hmci)
  HTTP Method: Select GET
  - c. Provide a Min time interval (default is 10s).
  - d. Create Custom HTTP Headers where Header=Authorization and Value=Token <your token> [ make sure that you are adding space between Token word and your token of InfluxDB]
  like: `Token hTHG-mwhRypjO8nZEmdzVKL4fM7kJH7989MC9JdgXacVHfBsks8AzeIwhqv-sXm76dphjO5pvqv5Fmsvw_zvGA==`
6. Press Save and test, 


## Troubelshooting

> :bulb: If there is no data in your Grafana dashboard, please wait 5 min, then check that your timezone and date/time  is correct/same in both the Container. Default it's UTC. 
    See example here for how to set timezone in container. https://gist.github.com/sjimenez44/1b73afeae3eec26a1915b0d4d5873b8f

If you have issue creating the network, try manually
```
docker network create external_network
```

To get the DNS to work internally between containers and running podman, you will need to install podman-plugin


```
yum install podman-plugin
podman network create metric-exporter-compose
```
When I ran yum install podman-plugins it included the dnsmasq for DNS resolution inside the podman network.
Check that   `"dns_enabled": true,`

```bash 
# podman network inspect metric-exporter-compose
[
     {
          "name": "metric-exporter-compose",
          "id": "572fd022aabfc7b2a338210218c3dcb541ca3d4e9ef9780850d26052c0eb4131",
          "driver": "bridge",
          "network_interface": "cni-podman1",
          "created": "2024-06-17T17:05:40.330948421+02:00",
          "subnets": [
               {
                    "subnet": "10.89.0.0/24",
                    "gateway": "10.89.0.1"
               }
          ],
          "ipv6_enabled": false,
          "internal": false,
          "dns_enabled": true,
          "ipam_options": {
               "driver": "host-local"
          }
     }
]
```