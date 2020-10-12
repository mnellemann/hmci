package biz.nellemann.hmci.pcm;

import com.serjltt.moshi.adapters.FirstElement;

public class SharedAdapter {

    public String id;
    public String type;
    public String physicalLocation;

    @FirstElement
    public Number receivedPackets;

    @FirstElement
    public Number sentPackets;

    @FirstElement
    public Number droppedPackets;

    @FirstElement
    public Number sentBytes;

    @FirstElement
    public Number receivedBytes;

    @FirstElement
    public Number transferredBytes;

    @FirstElement
    public String bridgedAdapters;

}
