package biz.nellemann.hmci.dto.json;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Storage adapter
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public final class FiberChannelAdapter {

    public String id;
    public String wwpn;
    public String physicalLocation;

    public int numOfPorts;
    public double numOfReads;
    public double numOfWrites;
    public double readBytes;
    public double writeBytes;
    public double runningSpeed;
    public double transmittedBytes;

}
