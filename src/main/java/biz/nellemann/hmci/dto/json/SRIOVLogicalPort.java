package biz.nellemann.hmci.dto.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SRIOVLogicalPort {

    public String drcIndex;
    public String physicalLocation;
    public String physicalDrcIndex;
    public int physicalPortId;
    public String clientPartitionUUID;
    public String vnicDeviceMode;
    public String configurationType;
    public double receivedPackets;
    public double sentPackets;
    public double droppedPackets;
    public double sentBytes;
    public double receivedBytes;
    public Number errorIn;
    public Number errorOut;
    public Number transferredBytes;

}
