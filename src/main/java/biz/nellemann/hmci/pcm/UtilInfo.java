package biz.nellemann.hmci.pcm;

import com.serjltt.moshi.adapters.FirstElement;

import java.util.ArrayList;
import java.util.List;

public class UtilInfo {

    public String version = "";
    public String metricType = "";
    public Integer frequency = 0;
    public String startTimeStamp = "";
    public String endTimeStamp = "";
    public String mtms = "";
    public String name = "";
    public String uuid = "";

    @FirstElement
    public String metricArrayOrder = "";

}
