package biz.nellemann.hmci.pcm;

import java.util.List;

public class FiberChannelAdapter {

    String id;
    String wwpn;
    String physicalLocation;
    Integer numOfPorts;
    List<Number> numOfReads;
    List<Number> numOfWrites;
    List<Number> readBytes;
    List<Number> writeBytes;
    List<Number> runningSpeed;
    List<Number> transmittedBytes;

}
