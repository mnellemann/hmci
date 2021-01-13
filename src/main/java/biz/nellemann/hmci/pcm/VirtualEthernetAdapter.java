package biz.nellemann.hmci.pcm;

import com.serjltt.moshi.adapters.FirstElement;


public class VirtualEthernetAdapter {

    public String physicalLocation = "";
    public Integer vlanId = 0;
    public Integer vswitchId = 0;
    public Boolean isPortVlanId = false;   
    public Integer viosId = 0;
    public String sharedEthernetAdapterId = "";

    @FirstElement
    public Number receivedPackets = 0;

    @FirstElement
    public Number sentPackets = 0;

    @FirstElement
    public Number droppedPackets = 0;

    @FirstElement
    public Number sentBytes = 0;

    @FirstElement
    public Number receivedBytes = 0;

    @FirstElement
    public Number receivedPhysicalPackets = 0;

    @FirstElement
    public Number sentPhysicalPackets = 0;

    @FirstElement
    public Number droppedPhysicalPackets = 0;

    @FirstElement
    public Number sentPhysicalBytes = 0;

    @FirstElement
    public Number receivedPhysicalBytes = 0;

    @FirstElement
    public Number transferredBytes = 0;

    @FirstElement
    public Number transferredPhysicalBytes = 0;

}
