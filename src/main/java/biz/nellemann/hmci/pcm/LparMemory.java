package biz.nellemann.hmci.pcm;

import com.serjltt.moshi.adapters.FirstElement;

public class LparMemory {

    @FirstElement
    public Number logicalMem;

    @FirstElement
    public Number utilizedMem;

    @FirstElement
    public Number backedPhysicalMem;

}
