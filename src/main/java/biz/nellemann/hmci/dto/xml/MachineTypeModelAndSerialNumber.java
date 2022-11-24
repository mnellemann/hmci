package biz.nellemann.hmci.dto.xml;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties({ "kb", "kxe", "schemaVersion", "Metadata" })
public class MachineTypeModelAndSerialNumber implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("MachineType")
    public String machineType;

    public String getMachineType() {
        return machineType;
    }

    @JsonProperty("Model")
    public String model;

    public String getModel() {
        return model;
    }

    @JsonProperty("SerialNumber")
    public String serialNumber;

    public String getSerialNumber() {
        return serialNumber;
    }

}
