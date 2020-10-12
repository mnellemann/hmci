package biz.nellemann.hmci.pcm;

import com.serjltt.moshi.adapters.FirstElement;

public class FiberChannelAdapter {

    public String id;
    public String wwpn;
    public String physicalLocation;
    public Integer numOfPorts;

    @FirstElement
    public Number numOfReads;

    @FirstElement
    public Number numOfWrites;

    @FirstElement
    public Number readBytes;

    @FirstElement
    public Number writeBytes;

    @FirstElement
    public Number runningSpeed;

    @FirstElement
    public Number transmittedBytes;

}
