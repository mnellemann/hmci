package biz.nellemann.hmci.pcm;

import java.util.ArrayList;
import java.util.List;

public class GenericAdapter {

    public String id = "";
    public String type = "";
    public String physicalLocation = "";
    public List<Number> receivedPackets = new ArrayList<>();
    public List<Number> sentPackets = new ArrayList<>();
    public List<Number> droppedPackets = new ArrayList<>();
    public List<Number> sentBytes = new ArrayList<>();
    public List<Number> receivedBytes = new ArrayList<>();
    public List<Number> transferredBytes = new ArrayList<>();

}
