package biz.nellemann.hmci.dto.json;


import java.util.ArrayList;
import java.util.List;

public final class Network {

    public List<String> clientLpars = new ArrayList<>();
    public List<GenericAdapter> genericAdapters = new ArrayList<>();
    public List<SharedAdapter> sharedAdapters = new ArrayList<>();
    public List<VirtualEthernetAdapter> virtualEthernetAdapters = new ArrayList<>();
    public List<SRIOVAdapter> sriovAdapters = new ArrayList<>();
    public List<SRIOVLogicalPort> sriovLogicalPorts = new ArrayList<>();

}
