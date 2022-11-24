package biz.nellemann.hmci.dto.json;


/**
 * Network adapter SEA
 */

public final class VirtualEthernetAdapter {

    public String physicalLocation = "";
    public Integer vlanId = 0;
    public Integer vswitchId = 0;
    public Boolean isPortVlanId = false;
    public Integer viosId = 0;
    public String sharedEthernetAdapterId = "";

    public Double receivedPackets = 0.0;
    public Double sentPackets = 0.0;
    public Double droppedPackets = 0.0;
    public Double sentBytes = 0.0;
    public Double receivedBytes = 0.0;
    public Double receivedPhysicalPackets = 0.0;
    public Double sentPhysicalPackets = 0.0;
    public Double droppedPhysicalPackets = 0.0;
    public Double sentPhysicalBytes = 0.0;
    public Double receivedPhysicalBytes = 0.0;
    public Double transferredBytes = 0.0;
    public Double transferredPhysicalBytes = 0.0;

}
