package biz.nellemann.hmci.dto.toml;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Configuration {

    public InfluxConfiguration influx;
    public PrometheusConfiguration prometheus;
    public Map<String, HmcConfiguration> hmc;

}
