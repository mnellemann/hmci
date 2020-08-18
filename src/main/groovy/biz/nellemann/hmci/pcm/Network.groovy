package biz.nellemann.hmci.pcm

import groovy.transform.ToString

@ToString
class Network {
    List<GenericAdapter> genericAdapters
    List<SharedAdapter> sharedAdapters
    List<VirtualEthernetAdapter> virtualEthernetAdapters
}
