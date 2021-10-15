package biz.nellemann.hmci.pcm;

import java.util.ArrayList;
import java.util.List;

public final class Storage {

    public final List<String> clientLpars = new ArrayList<>();
    public final List<GenericPhysicalAdapters> genericPhysicalAdapters = new ArrayList<>();
    public final List<GenericVirtualAdapter> genericVirtualAdapters = new ArrayList<>();
    public final List<FiberChannelAdapter> fiberChannelAdapters = new ArrayList<>();
    public final List<VirtualFiberChannelAdapter> virtualFiberChannelAdapters = new ArrayList<>();

}
