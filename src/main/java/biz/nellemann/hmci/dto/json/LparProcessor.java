package biz.nellemann.hmci.dto.json;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class LparProcessor {

    public int poolId = 0;
    public int weight = 0;
    public String mode = "";
    public double maxVirtualProcessors = 0.0;
    public double currentVirtualProcessors = 0.0;
    public double maxProcUnits = 0.0;
    public double entitledProcUnits = 0.0;
    public double utilizedProcUnits = 0.0;
    public double utilizedCappedProcUnits = 0.0;
    public double utilizedUncappedProcUnits = 0.0;
    public double idleProcUnits = 0.0;
    public double donatedProcUnits = 0.0;
    public double timeSpentWaitingForDispatch = 0.0;
    public double timePerInstructionExecution = 0.0;

}
