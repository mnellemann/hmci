package biz.nellemann.hmci.pcm;

import com.serjltt.moshi.adapters.FirstElement;

public final class FiberChannelAdapter {

    public String id = "";
    public String wwpn = "";
    public String physicalLocation = "";
    public Integer numOfPorts = 0;

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
