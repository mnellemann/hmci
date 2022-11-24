package biz.nellemann.hmci.dto.json;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

public final class SystemFirmware {

    @JsonUnwrapped
    public Double utilizedProcUnits;// = 0.0;

    public Double assignedMem = 0.0;

}
