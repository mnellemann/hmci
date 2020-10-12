package biz.nellemann.hmci.pcm;

import com.serjltt.moshi.adapters.FirstElement;

public class VirtualEthernetAdapter {

    public String physicalLocation;
    public Integer vlanId;
    public Integer vswitchId;
    public Boolean isPortVlanId;
    public Integer viosId;
    public String sharedEthernetAdapterId;

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
    public Number receivedPhysicalPackets;

    @FirstElement
    public Number sentPhysicalPackets;

    @FirstElement
    public Number droppedPhysicalPackets;

    @FirstElement
    public Number sentPhysicalBytes;

    @FirstElement
    public Number receivedPhysicalBytes;

    @FirstElement
    public Number transferredBytes;

    @FirstElement
    public Number transferredPhysicalBytes;

}
