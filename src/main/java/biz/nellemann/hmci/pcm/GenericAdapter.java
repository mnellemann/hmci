package biz.nellemann.hmci.pcm;

import java.util.List;

public class GenericAdapter {

    String id;
    String type;
    String physicalLocation;
    List<Number> receivedPackets;
    List<Number> sentPackets;
    List<Number> droppedPackets;
    List<Number> sentBytes;
    List<Number> receivedBytes;
    List<Number> transferredBytes;

}
