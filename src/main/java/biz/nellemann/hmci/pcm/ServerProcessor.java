package biz.nellemann.hmci.pcm;

import com.serjltt.moshi.adapters.FirstElement;

public class ServerProcessor {

    @FirstElement
    public Number totalProcUnits = 0;

    @FirstElement
    public Number utilizedProcUnits = 0;

    @FirstElement
    public Number availableProcUnits = 0;

    @FirstElement
    public Number configurableProcUnits = 0;

}
