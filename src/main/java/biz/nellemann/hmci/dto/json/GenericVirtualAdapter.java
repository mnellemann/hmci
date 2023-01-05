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
    public Double numOfReads = 0.0;
    public Double numOfWrites = 0.0;
    public Double readBytes = 0.0;
    public Double writeBytes = 0.0;
    public Double transmittedBytes = 0.0;

}
