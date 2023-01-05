package biz.nellemann.hmci.dto.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class ServerMemory {

    public double totalMem = 0.0;
    public double availableMem = 0.0;
    public double configurableMem = 0.0;
    public double assignedMemToLpars = 0.0;
    public double virtualPersistentMem = 0.0;

}
