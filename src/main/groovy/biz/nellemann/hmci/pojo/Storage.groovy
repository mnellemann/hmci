package biz.nellemann.hmci.pojo

import groovy.transform.ToString

@ToString
class Storage {

    List<String> clientLpars
    List<GenericPhysicalAdapters> genericPhysicalAdapters
    List<GenericVirtualAdapter> genericVirtualAdapters
    List<VirtualFiberChannelAdapter> virtualFiberChannelAdapters
    List<FiberChannelAdapter> fiberChannelAdapters

}
