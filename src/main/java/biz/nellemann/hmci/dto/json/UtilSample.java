package biz.nellemann.hmci.dto.json;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class UtilSample {

    public String sampleType = "";

    @JsonProperty("sampleInfo")
    public SampleInfo sampleInfo = new SampleInfo();

    public SampleInfo getInfo() {
        return sampleInfo;
    }


    @JsonProperty("systemFirmwareUtil")
    public SystemFirmware systemFirmwareUtil = new SystemFirmware();

    public ServerUtil serverUtil = new ServerUtil();
    public EnergyUtil energyUtil = new EnergyUtil();
    public List<ViosUtil> viosUtil = new ArrayList<>();
    public LparUtil lparsUtil = new LparUtil();

}
