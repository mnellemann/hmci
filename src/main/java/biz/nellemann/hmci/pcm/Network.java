package biz.nellemann.hmci.pcm;

import java.util.ArrayList;
import java.util.List;

public final class Network {

    public final List<String> clientLpars  = new ArrayList<>();
    public final List<GenericAdapter> genericAdapters = new ArrayList<>();
    public final List<SharedAdapter> sharedAdapters = new ArrayList<>();
    public final List<VirtualEthernetAdapter> virtualEthernetAdapters = new ArrayList<>();

    public final List<SriovLogicalPort> sriovLogicalPorts = new ArrayList<>();

}
