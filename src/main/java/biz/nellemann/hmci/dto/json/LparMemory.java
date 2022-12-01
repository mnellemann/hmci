package biz.nellemann.hmci.dto.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class LparMemory {

    public Double logicalMem;
    public Double utilizedMem = 0.0;
    public Double backedPhysicalMem = 0.0;

}
