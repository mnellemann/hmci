package biz.nellemann.hmci.pcm;

import com.serjltt.moshi.adapters.FirstElement;

public class LparProcessor {

    public Integer poolId = 0;
    public Integer weight = 0;
    public String mode = "";

    @FirstElement
    public Number maxVirtualProcessors;

    @FirstElement
    public Number currentVirtualProcessors;

    @FirstElement
    public Number maxProcUnits;

    @FirstElement
    public Number entitledProcUnits;

    @FirstElement
    public Number utilizedProcUnits;

    @FirstElement
    public Number utilizedCappedProcUnits;

    @FirstElement
    public Number utilizedUncappedProcUnits;

    @FirstElement
    public Number idleProcUnits;

    @FirstElement
    public Number donatedProcUnits;

    @FirstElement
    public Number timeSpentWaitingForDispatch;

    @FirstElement
    public Number timePerInstructionExecution;

}
