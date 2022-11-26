package biz.nellemann.hmci.dto.xml;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "ManagedSystemPcmPreference:ManagedSystemPcmPreference")
public class ManagedSystemPcmPreference {

    @JacksonXmlProperty(isAttribute = true)
    private final String schemaVersion = "V1_0";

    @JacksonXmlProperty(isAttribute = true, localName = "xmlns:ManagedSystemPcmPreference")
    private final String xmlns = "http://www.ibm.com/xmlns/systems/power/firmware/pcm/mc/2012_10/";

    @JsonProperty("Metadata")
    public Metadata metadata;

    @JsonProperty("SystemName")
    public String systemName;

    //public MachineTypeModelAndSerialNumber machineTypeModelSerialNumber;

    @JsonProperty("EnergyMonitoringCapable")
    public Boolean energyMonitoringCapable = false;

    @JsonProperty("LongTermMonitorEnabled")
    public Boolean longTermMonitorEnabled = false;

    @JsonProperty("AggregationEnabled")
    public Boolean aggregationEnabled = false;

    @JsonProperty("ShortTermMonitorEnabled")
    public Boolean shortTermMonitorEnabled;

    @JsonProperty("ComputeLTMEnabled")
    public Boolean computeLTMEnabled;

    @JsonProperty("EnergyMonitorEnabled")
    public Boolean energyMonitorEnabled;


}
