package biz.nellemann.hmci.pcm;

import com.serjltt.moshi.adapters.FirstElement;

public final class PhysicalProcessorPool {

    @FirstElement
    public Number assignedProcUnits = 0.0;

    @FirstElement
    public Number utilizedProcUnits = 0.0;

    @FirstElement
    public Number availableProcUnits = 0.0;

    @FirstElement
    public Number configuredProcUnits = 0.0;

    @FirstElement
    public Number borrowedProcUnits = 0.0;

}
