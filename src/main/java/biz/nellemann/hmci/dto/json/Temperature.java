package biz.nellemann.hmci.dto.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class Temperature {

    public String entityId = "";
    public String entityInstance = "";
    public Double temperatureReading = 0.0;

}
