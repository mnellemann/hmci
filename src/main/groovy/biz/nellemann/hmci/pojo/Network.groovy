package biz.nellemann.hmci.pojo

import groovy.transform.ToString

@ToString
class Network {
    List<GenericAdapter> genericAdapters
    List<SharedAdapter> sharedAdapters
    List<VirtualEthernetAdapter> virtualEthernetAdapters
}
