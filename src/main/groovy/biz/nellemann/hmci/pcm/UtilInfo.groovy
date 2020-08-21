package biz.nellemann.hmci.pcm

import groovy.transform.ToString

@ToString
class UtilInfo {

    String version
    String metricType
    Integer frequency
    String startTimeStamp
    String endTimeStamp
    String mtms
    String name
    String uuid
    List<String> metricArrayOrder

}
