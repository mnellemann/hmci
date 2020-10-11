package biz.nellemann.hmci.pcm;

import com.serjltt.moshi.adapters.FirstElement;
import com.squareup.moshi.Json;

import java.util.List;

public class ServerMemory {

    @FirstElement
    public Number totalMem;

    @FirstElement
    public Number availableMem;

    @FirstElement
    public Number configurableMem;

    @FirstElement
    public Number assignedMemToLpars;

}
