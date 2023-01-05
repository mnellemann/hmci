package biz.nellemann.hmci.dto.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class EnergyUtil {

    public PowerUtil powerUtil = new PowerUtil();
    public ThermalUtil thermalUtil = new ThermalUtil();

}
