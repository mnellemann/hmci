# HMC Insights

Small Java-based utility to fetch metrics from one or more HMC's and push those to an InfluxDB time-series database. 

## TODO Liste

- Use TOML for configuration file, to support multiple HMC's - https://github.com/tomlj/tomlj


## Usage Instructions

...

## Development Information


### InfluxDB for test and development

Start the influxdb container

    docker run --name=influxdb -d -p 8086:8086 influxdb


Use the influx client from the container

    docker exec -it influxdb influx
