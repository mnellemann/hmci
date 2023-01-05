package biz.nellemann.hmci.dto.json;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class SharedProcessorPool {

    public int id;
    public String name;

    public double assignedProcUnits = 0.0;
    public double utilizedProcUnits = 0.0;
    public double availableProcUnits = 0.0;
    public double configuredProcUnits = 0.0;
    public double borrowedProcUnits = 0.0;

}
