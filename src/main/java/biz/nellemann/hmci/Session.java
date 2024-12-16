package biz.nellemann.hmci;

import biz.nellemann.hmci.dto.toml.HmcConfiguration;

import java.util.List;

public class Session {

    private RestClient restClient;
    private InfluxClient influxClient;
    private PrometheusClient prometheusClient;

    Boolean doEnergy;
    Integer refreshValue;
    Integer discoverValue;
    List<String> excludeSystems;
    List<String> includeSystems;
    List<String> excludePartitions;
    List<String> includePartitions;


    Session() {
    }

    Session(HmcConfiguration configuration) {
        this.refreshValue = configuration.refresh;
        this.discoverValue = configuration.discover;
        this.doEnergy = configuration.energy;
        restClient = new RestClient(configuration.url, configuration.username, configuration.password, configuration.trust, configuration.timeout);

        this.excludeSystems = configuration.excludeSystems;
        this.includeSystems = configuration.includeSystems;
        this.excludePartitions = configuration.excludePartitions;
        this.includePartitions = configuration.includePartitions;
    }


    public RestClient getRestClient() {
        return restClient;
    }

    public void setRestClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public void setInfluxClient(InfluxClient influxClient) {
        this.influxClient = influxClient;
    }

    public void setPrometheusClient(PrometheusClient prometheusClient) {
        this.prometheusClient = prometheusClient;
    }

    public void writeMetric(List<MeasurementBundle> bundle) {
        if(influxClient != null) {
            influxClient.write(bundle);
        }
        if(prometheusClient != null) {
            prometheusClient.write(bundle);
        }
    }


}
