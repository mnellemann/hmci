package biz.nellemann.hmci.dto.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class ServerProcessor {

    public Double totalProcUnits = 0.0;
    public Double utilizedProcUnits = 0.0;
    public Double availableProcUnits = 0.0;
    public Double configurableProcUnits = 0.0;

}
