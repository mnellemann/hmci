package biz.nellemann.hmci.dto.json;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class SystemFirmware {

    @JsonUnwrapped
    public double utilizedProcUnits; // = 0.0;

    public double assignedMem = 0.0;

}
