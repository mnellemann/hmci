package biz.nellemann.hmci.dto.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class LparMemory {

    public double logicalMem;
    public double utilizedMem = 0.0;
    public double backedPhysicalMem = 0.0;

}
