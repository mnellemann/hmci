package biz.nellemann.hmci.dto.json;


public final class LparProcessor {

    public Integer poolId = 0;
    public Integer weight = 0;
    public String mode = "";
    public Double maxVirtualProcessors = 0.0;
    public Double currentVirtualProcessors = 0.0;
    public Double maxProcUnits = 0.0;
    public Double entitledProcUnits = 0.0;
    public Double utilizedProcUnits = 0.0;
    public Double utilizedCappedProcUnits = 0.0;
    public Double utilizedUncappedProcUnits = 0.0;
    public Double idleProcUnits = 0.0;
    public Double donatedProcUnits = 0.0;
    public Double timeSpentWaitingForDispatch = 0.0;
    public Double timePerInstructionExecution = 0.0;

}
