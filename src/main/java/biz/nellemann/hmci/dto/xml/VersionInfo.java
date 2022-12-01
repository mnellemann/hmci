package biz.nellemann.hmci.dto.xml;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties({ "kxe", "kb", "schemaVersion", "Metadata" })
public class VersionInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("BuildLevel")
    public String buildLevel;

    @JsonProperty("Maintenance")
    protected String maintenance;

    @JsonProperty("Minor")
    protected String minor;

    @JsonProperty("Release")
    protected String release;

    @JsonProperty("ServicePackName")
    public String servicePackName;

    @JsonProperty("Version")
    protected String version;

}
