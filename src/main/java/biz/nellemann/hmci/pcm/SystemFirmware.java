package biz.nellemann.hmci.pcm;

import com.serjltt.moshi.adapters.FirstElement;

public final class SystemFirmware {

    @FirstElement
    public Number utilizedProcUnits = 0.0;

    @FirstElement
    public Number assignedMem = 0.0;

}
