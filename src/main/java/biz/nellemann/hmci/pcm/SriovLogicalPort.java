package biz.nellemann.hmci.pcm;

import com.serjltt.moshi.adapters.FirstElement;

public class SriovLogicalPort {

    public String drcIndex = "";

    public String physicalLocation = "";        // "U78CA.001.CSS0CXA-P1-C2-C1-T1-S2"

    public String physicalDrcIndex = "";

    public Number physicalPortId = 0;

    public String vnicDeviceMode = "";          // "NonVNIC"

    public String configurationType = "";       // "Ethernet"


    @FirstElement
    public Number receivedPackets = 0.0;

    @FirstElement
    public Number sentPackets = 0.0;

    @FirstElement
    public Number droppedPackets = 0.0;

    @FirstElement
    public Number sentBytes = 0.0;

    @FirstElement
    public Number receivedBytes = 0.0;

    @FirstElement
    public Number errorIn = 0.0;

    @FirstElement
    public Number errorOut = 0.0;

    @FirstElement
    public Number transferredBytes = 0.0;

}
