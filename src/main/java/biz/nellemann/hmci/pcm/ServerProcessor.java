package biz.nellemann.hmci.pcm;

import com.serjltt.moshi.adapters.FirstElement;

public class ServerProcessor {

    @FirstElement
    public Number totalProcUnits;

    @FirstElement
    public Number utilizedProcUnits;

    @FirstElement
    public Number availableProcUnits;

    @FirstElement
    public Number configurableProcUnits;

}
