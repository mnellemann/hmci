package biz.nellemann.hmci.pcm;

import com.serjltt.moshi.adapters.FirstElement;

import java.util.ArrayList;
import java.util.List;

public class VirtualFiberChannelAdapter {

    public String wwpn = "";
    public String wwpn2 = "";
    public String physicalLocation = "";
    public String physicalPortWWPN = "";
    public Integer viosId = 0;

    @FirstElement
    public Number numOfReads = 0.0;

    @FirstElement
    public Number numOfWrites = 0.0;

    @FirstElement
    public Number readBytes = 0.0;

    @FirstElement
    public Number writeBytes = 0.0;

    @FirstElement
    public Number runningSpeed = 0.0;

    @FirstElement
    public Number transmittedBytes = 0.0;

}
