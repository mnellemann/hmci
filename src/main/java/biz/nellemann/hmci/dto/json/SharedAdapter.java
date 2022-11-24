package biz.nellemann.hmci.dto.json;


import java.util.List;

/**
 * Network adapter
 */

public final class SharedAdapter {

    public String id;
    public String type;
    public String physicalLocation;

    public double receivedPackets;
    public double sentPackets;
    public double droppedPackets;
    public double sentBytes;
    public double receivedBytes;
    public double transferredBytes;

    public List<String> bridgedAdapters;

}
