package biz.nellemann.hmci.dto.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class SRIOVAdapter {

    public String drcIndex = "";

    public List<SRIOVPhysicalPort> physicalPorts;

}
