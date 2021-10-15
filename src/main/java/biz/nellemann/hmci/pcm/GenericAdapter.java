package biz.nellemann.hmci.pcm;

import com.serjltt.moshi.adapters.FirstElement;

public final class GenericAdapter {

    public String id = "";
    public String type = "";
    public String physicalLocation = "";

    @FirstElement
    public Number receivedPackets = 0.0;

    @FirstElement
    public Number sentPackets = 0.0;

    @FirstElement
    public Number droppedPackets = 0.0;

    @FirstElement
    public Number sentBytes = 0.0;

    @FirstElement
    public Number receivedBytes = 0.0;

    @FirstElement
    public Number transferredBytes = 0.0;

}
