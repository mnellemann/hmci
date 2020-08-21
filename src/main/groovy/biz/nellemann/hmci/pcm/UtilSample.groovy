package biz.nellemann.hmci.pcm

import groovy.transform.ToString

@ToString
class UtilSample {

    String sampleType
    SampleInfo sampleInfo
    ServerUtil serverUtil
    List<ViosUtil> viosUtil
    List<LparUtil> lparsUtil

}
