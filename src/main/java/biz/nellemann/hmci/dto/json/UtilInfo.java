package biz.nellemann.hmci.dto.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({ "metricArrayOrder" })
public final class UtilInfo {

    public String version = "";
    public String metricType = "";
    public Integer frequency = 0;
    public String startTimeStamp = "";
    public String endTimeStamp = "";
    public String mtms = "";
    public String name = "";
    public String uuid = "";

}
