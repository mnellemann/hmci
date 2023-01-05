package biz.nellemann.hmci.dto.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class ThermalUtil {

    public List<Temperature> inletTemperatures = new ArrayList<>();
    public List<Temperature> cpuTemperatures = new ArrayList<>();
    public List<Temperature> baseboardTemperatures = new ArrayList<>();

}
