package biz.nellemann.hmci.pcm;

import com.serjltt.moshi.adapters.FirstElement;

public class SharedProcessorPool {

    public String id = "";
    public String name = "";

    @FirstElement
    public Number assignedProcUnits = 0;

    @FirstElement
    public Number utilizedProcUnits = 0;

    @FirstElement
    public Number availableProcUnits = 0;

    @FirstElement
    public Number configuredProcUnits = 0;

    @FirstElement
    public Number borrowedProcUnits = 0;

}
