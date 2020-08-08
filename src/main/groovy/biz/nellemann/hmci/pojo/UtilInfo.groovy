package biz.nellemann.hmci.pojo

import groovy.transform.ToString

import java.time.Instant

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
