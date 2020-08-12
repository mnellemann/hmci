# HMC Insights

Small Java-based utility to fetch metrics from one or more HMC's and push those to an InfluxDB time-series database. 

## TODO Liste

- Use TOML for configuration file, to support multiple HMC's - https://github.com/tomlj/tomlj


## Usage Instructions

...

## Development Information

### Build &amp; Test

Use the gradle build tool

    ./gradlew clean build
    

### InfluxDB for local testing

Start the InfluxDB container

    docker run --name=influxdb -d -p 8086:8086 influxdb

To use the Influx client from the same container

    docker exec -it influxdb influx


### Grafana for local testing 

Start the Grafana container, linking it to the InfluxDB container

    docker run --name grafana --link influxdb:influxdb -d -p 3000:3000 grafana/grafana:7.1.3
    
Configure a new InfluxDB datasource on **http://influxdb:8086** to talk to the InfluxDB container. The database must be created beforehand, this can be done by running the hmci tool.