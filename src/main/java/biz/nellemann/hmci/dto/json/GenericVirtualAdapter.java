package biz.nellemann.hmci.dto.json;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Storage adapter
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public final class GenericVirtualAdapter {

    public String id = "";
    public String type = "";
    public Integer viosId = 0;
    public String physicalLocation = "";
    public double numOfReads = 0.0;
    public double numOfWrites = 0.0;
    public double readBytes = 0.0;
    public double writeBytes = 0.0;
    public double transmittedBytes = 0.0;

}
