package biz.nellemann.hmci.pcm;

import com.serjltt.moshi.adapters.FirstElement;

public class ServerMemory {

    @FirstElement
    public Number totalMem = 0.0;

    @FirstElement
    public Number availableMem = 0.0;

    @FirstElement
    public Number configurableMem = 0.0;

    @FirstElement
    public Number assignedMemToLpars = 0.0;

}
