package biz.nellemann.hmci.pcm;

import java.util.ArrayList;
import java.util.List;

public class PhysicalProcessorPool {

    public List<Number> assignedProcUnits = new ArrayList<>();
    public List<Number> utilizedProcUnits = new ArrayList<>();
    public List<Number> availableProcUnits = new ArrayList<>();
    public List<Number> configuredProcUnits = new ArrayList<>();
    public List<Number> borrowedProcUnits = new ArrayList<>();

}
