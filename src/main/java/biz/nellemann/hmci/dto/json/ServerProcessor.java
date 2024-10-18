package biz.nellemann.hmci.dto.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class ServerProcessor {

    public double totalProcUnits = 0.0;
    public double utilizedProcUnits = 0.0;
    public double availableProcUnits = 0.0;
    public double configurableProcUnits = 0.0;

}
