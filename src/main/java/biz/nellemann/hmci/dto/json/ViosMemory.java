package biz.nellemann.hmci.dto.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class ViosMemory {
    public double assignedMem;
    public double utilizedMem;
    public double virtualPersistentMem;
}
