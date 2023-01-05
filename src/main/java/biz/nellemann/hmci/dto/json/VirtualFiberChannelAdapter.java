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
    public Integer viosId = 0;

    public Double numOfReads = 0.0;
    public Double numOfWrites = 0.0;
    public Double readBytes = 0.0;
    public Double writeBytes = 0.0;
    public Double runningSpeed = 0.0;
    public Double transmittedBytes = 0.0;

}
