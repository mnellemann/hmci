package biz.nellemann.hmci.pcm;

import java.util.List;

public class VirtualEthernetAdapter {

    String physicalLocation;
    Integer vlanId;
    Integer vswitchId;
    Boolean isPortVlanId;
    Integer viosId;
    String sharedEthernetAdapterId;
    List<Number> receivedPackets;
    List<Number> sentPackets;
    List<Number> droppedPackets;
    List<Number> sentBytes;
    List<Number> receivedBytes;
    List<Number> receivedPhysicalPackets;
    List<Number> sentPhysicalPackets;
    List<Number> droppedPhysicalPackets;
    List<Number> sentPhysicalBytes;
    List<Number> receivedPhysicalBytes;
    List<Number> transferredBytes;
    List<Number> transferredPhysicalBytes;

}
