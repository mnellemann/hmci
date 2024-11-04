package biz.nellemann.hmci.dto.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class SRIOVPhysicalPort {

    public String id;
    public String physicalLocation = "";        // "U78CA.001.CSS0CXA-P1-C2-C1-T1-S2"
    public String physicalDrcIndex = "";
    public int physicalPortId = 0;
    public String vnicDeviceMode = "";          // "NonVNIC"
    public String configurationType = "";       // "Ethernet"
    public double receivedPackets = 0.0;
    public double sentPackets = 0.0;
    public double droppedPackets = 0.0;
    public double sentBytes = 0.0;
    public double receivedBytes = 0.0;
    public double errorIn = 0.0;
    public double errorOut = 0.0;
    public double transferredBytes = 0.0;

}
