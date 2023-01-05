package biz.nellemann.hmci.dto.json;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class Network {

    public List<String> clientLpars = new ArrayList<>();
    public List<GenericAdapter> genericAdapters = new ArrayList<>();
    public List<SharedAdapter> sharedAdapters = new ArrayList<>();
    public List<VirtualEthernetAdapter> virtualEthernetAdapters = new ArrayList<>();
    public List<SRIOVAdapter> sriovAdapters = new ArrayList<>();
    public List<SRIOVLogicalPort> sriovLogicalPorts = new ArrayList<>();

}
