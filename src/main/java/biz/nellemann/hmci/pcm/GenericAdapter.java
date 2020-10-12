package biz.nellemann.hmci.pcm;

import java.util.List;

public class GenericAdapter {

    public String id;
    public String type;
    public String physicalLocation;
    public List<Number> receivedPackets;
    public List<Number> sentPackets;
    public List<Number> droppedPackets;
    public List<Number> sentBytes;
    public List<Number> receivedBytes;
    public List<Number> transferredBytes;

}
