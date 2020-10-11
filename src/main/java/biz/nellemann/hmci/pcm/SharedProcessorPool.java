package biz.nellemann.hmci.pcm;

import com.serjltt.moshi.adapters.FirstElement;

public class SharedProcessorPool {

    public String id;
    public String name;

    @FirstElement
    public Number assignedProcUnits;

    @FirstElement
    public Number utilizedProcUnits;

    @FirstElement
    public Number availableProcUnits;

    @FirstElement
    public Number configuredProcUnits;

    @FirstElement
    public Number borrowedProcUnits;

}
