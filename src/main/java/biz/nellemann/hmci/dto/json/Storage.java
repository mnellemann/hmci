package biz.nellemann.hmci.dto.json;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class Storage {

    public List<String> clientLpars = new ArrayList<>();
    public List<GenericPhysicalAdapters> genericPhysicalAdapters = new ArrayList<>();
    public List<GenericVirtualAdapter> genericVirtualAdapters = new ArrayList<>();
    public List<FiberChannelAdapter> fiberChannelAdapters = new ArrayList<>();
    public List<VirtualFiberChannelAdapter> virtualFiberChannelAdapters = new ArrayList<>();

}
