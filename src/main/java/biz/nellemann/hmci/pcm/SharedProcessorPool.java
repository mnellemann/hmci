package biz.nellemann.hmci.pcm;

import com.serjltt.moshi.adapters.FirstElement;

public final class SharedProcessorPool {

    public String id = "";
    public String name = "";

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
