package biz.nellemann.hmci.dto.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class PowerUtil {

    public double powerReading = 0.0;

}
