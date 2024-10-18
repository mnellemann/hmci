package biz.nellemann.hmci.dto.json;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Storage adapter - NPIV ?
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public final class VirtualFiberChannelAdapter {

    public String id = "";
    public String wwpn = "";
    public String wwpn2 = "";
    public String physicalLocation = "";
    public String physicalPortWWPN = "";
    public int viosId = 0;

    public double numOfReads = 0.0;
    public double numOfWrites = 0.0;
    public double readBytes = 0.0;
    public double writeBytes = 0.0;
    public double runningSpeed = 0.0;
    public double transmittedBytes = 0.0;

}
