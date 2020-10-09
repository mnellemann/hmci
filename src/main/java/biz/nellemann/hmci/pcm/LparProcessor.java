package biz.nellemann.hmci.pcm;

import java.util.List;

public class LparProcessor {

    Integer poolId;
    Integer weight;
    String mode;
    List<Number> maxVirtualProcessors;
    List<Number> currentVirtualProcessors;
    List<Number> maxProcUnits;
    List<Number> entitledProcUnits;
    List<Number> utilizedProcUnits;
    List<Number> utilizedCappedProcUnits;
    List<Number> utilizedUncappedProcUnits;
    List<Number> idleProcUnits;
    List<Number> donatedProcUnits;
    List<Number> timeSpentWaitingForDispatch;
    List<Number> timePerInstructionExecution;

}
