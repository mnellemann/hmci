package biz.nellemann.hmci.dto.json;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class GenericAdapter {

    public String id;
    public String type = "";
    public String physicalLocation = "";
    public double receivedPackets = 0.0;
    public double sentPackets = 0.0;
    public double droppedPackets = 0.0;
    public double sentBytes = 0.0;
    public double receivedBytes = 0.0;
    public double transferredBytes = 0.0;

}
