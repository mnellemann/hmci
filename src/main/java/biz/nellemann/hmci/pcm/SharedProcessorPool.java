package biz.nellemann.hmci.pcm;

import java.util.List;

public class SharedProcessorPool {

    String id;
    String name;
    List<Number> assignedProcUnits;
    List<Number> utilizedProcUnits;
    List<Number> availableProcUnits;
    List<Number> configuredProcUnits;
    List<Number> borrowedProcUnits;

}
