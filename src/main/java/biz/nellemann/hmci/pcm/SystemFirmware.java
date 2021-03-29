package biz.nellemann.hmci.pcm;

import com.serjltt.moshi.adapters.FirstElement;

import java.util.ArrayList;
import java.util.List;

public class SystemFirmware {

    @FirstElement
    public Number utilizedProcUnits = 0.0;

    @FirstElement
    public Number assignedMem = 0.0;

}
