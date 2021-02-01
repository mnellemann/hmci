package biz.nellemann.hmci.pcm;

import com.serjltt.moshi.adapters.FirstElement;

public class LparMemory {

    @FirstElement
    public Number logicalMem = 0.0;

    @FirstElement
    public Number utilizedMem = 0.0;

    @FirstElement
    public Number backedPhysicalMem = 0.0;

}
