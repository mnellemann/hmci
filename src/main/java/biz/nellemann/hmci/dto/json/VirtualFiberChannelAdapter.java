package biz.nellemann.hmci.dto.json;


/**
 * Storage adapter - NPIV ?
 */

public final class VirtualFiberChannelAdapter {

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
