package biz.nellemann.hmci.dto.xml;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.io.Serializable;

@JsonIgnoreProperties({ "kb", "kxe", "Metadata" })
public class MachineTypeModelAndSerialNumber implements Serializable {

    private static final long serialVersionUID = 1L;

    @JacksonXmlProperty(isAttribute = true)
    private final String schemaVersion = "V1_0";

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
