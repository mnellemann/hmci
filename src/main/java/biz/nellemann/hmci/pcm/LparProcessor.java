package biz.nellemann.hmci.pcm;

import com.serjltt.moshi.adapters.FirstElement;

public class LparProcessor {

    public Integer poolId = 0;
    public Integer weight = 0;
    public String mode = "";

    @FirstElement
    public Number maxVirtualProcessors = 0.0;

    @FirstElement
    public Number currentVirtualProcessors = 0.0;

    @FirstElement
    public Number maxProcUnits = 0.0;

    @FirstElement
    public Number entitledProcUnits = 0.0;

    @FirstElement
    public Number utilizedProcUnits = 0.0;

    @FirstElement
    public Number utilizedCappedProcUnits = 0.0;

    @FirstElement
    public Number utilizedUncappedProcUnits = 0.0;

    @FirstElement
    public Number idleProcUnits = 0.0;

    @FirstElement
    public Number donatedProcUnits = 0.0;

    @FirstElement
    public Number timeSpentWaitingForDispatch = 0.0;

    @FirstElement
    public Number timePerInstructionExecution = 0.0;

}
