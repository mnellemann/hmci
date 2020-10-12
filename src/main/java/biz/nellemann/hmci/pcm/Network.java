package biz.nellemann.hmci.pcm;

import java.util.ArrayList;
import java.util.List;

public class Network {

    public List<GenericAdapter> genericAdapters = new ArrayList<>();
    public List<SharedAdapter> sharedAdapters = new ArrayList<>();
    public List<VirtualEthernetAdapter> virtualEthernetAdapters = new ArrayList<>();

}
