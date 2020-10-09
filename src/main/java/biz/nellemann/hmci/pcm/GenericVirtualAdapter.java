package biz.nellemann.hmci.pcm;

import java.util.List;

public class GenericVirtualAdapter {

    String id;
    String type;
    Integer viosId;
    String physicalLocation;
    List<Number> numOfReads;
    List<Number> numOfWrites;
    List<Number> readBytes;
    List<Number> writeBytes;
    List<Number> transmittedBytes;

}
