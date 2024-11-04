package biz.nellemann.hmci.dto.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class UtilInfo {

    public String version = "";
    public String metricType = "";
    public int frequency = 0;
    public String startTimeStamp = "";
    public String endTimeStamp = "";
    public String mtms = "";
    public String name = "";
    public String uuid = "";

}
