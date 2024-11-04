package biz.nellemann.hmci.dto.json;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Network adapter SEA
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public final class VirtualEthernetAdapter {

    public String physicalLocation = "";
    public int vlanId = 0;
    public int vswitchId = 0;
    public boolean isPortVlanId = false;
    public Integer viosId = 0;
    public String sharedEthernetAdapterId = "";

    public double receivedPackets = 0.0;
    public double sentPackets = 0.0;
    public double droppedPackets = 0.0;
    public double sentBytes = 0.0;
    public double receivedBytes = 0.0;
    public double receivedPhysicalPackets = 0.0;
    public double sentPhysicalPackets = 0.0;
    public double droppedPhysicalPackets = 0.0;
    public double sentPhysicalBytes = 0.0;
    public double receivedPhysicalBytes = 0.0;
    public double transferredBytes = 0.0;
    public double transferredPhysicalBytes = 0.0;

}
