package biz.nellemann.hmci.dto.json;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class GenericPhysicalAdapters {

    public String id;
    public String type = "";
    public String physicalLocation;
    public double numOfReads;
    public double numOfWrites;
    public double readBytes;
    public double writeBytes;
    public double transmittedBytes;

}
