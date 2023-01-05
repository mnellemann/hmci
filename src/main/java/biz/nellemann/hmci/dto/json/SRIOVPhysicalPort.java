package biz.nellemann.hmci.dto.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class SRIOVPhysicalPort {

    public String id;
    public String physicalLocation = "";        // "U78CA.001.CSS0CXA-P1-C2-C1-T1-S2"
    public String physicalDrcIndex = "";
    public Number physicalPortId = 0;
    public String vnicDeviceMode = "";          // "NonVNIC"
    public String configurationType = "";       // "Ethernet"
    public Number receivedPackets = 0.0;
    public Number sentPackets = 0.0;
    public Number droppedPackets = 0.0;
    public Number sentBytes = 0.0;
    public Number receivedBytes = 0.0;
    public Number errorIn = 0.0;
    public Number errorOut = 0.0;
    public Number transferredBytes = 0.0;

}
