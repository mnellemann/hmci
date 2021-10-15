package biz.nellemann.hmci.pcm;

import com.serjltt.moshi.adapters.FirstElement;

public final class ViosMemory {

    @FirstElement
    public Number assignedMem = 0.0;

    @FirstElement
    public Number utilizedMem = 0.0;

}
