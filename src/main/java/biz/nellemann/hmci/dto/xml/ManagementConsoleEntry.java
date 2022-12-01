package biz.nellemann.hmci.dto.xml;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/*
@JsonIgnoreProperties({
    "schemaVersion", "Metadata", "NetworkInterfaces", "Driver", "LicenseID", "LicenseFirstYear", "UVMID",
    "TemplateObjectModelVersion", "UserObjectModelVersion", "WebObjectModelVersion", "PublicSSHKeyValue",
    "MinimumKeyStoreSize", "MinimumKeyStoreSize"
})*/
@JsonIgnoreProperties(ignoreUnknown = true)
public class ManagementConsoleEntry implements Serializable, ResourceEntry {

    private static final long serialVersionUID = 1L;

    @JsonProperty("MachineTypeModelAndSerialNumber")
    private MachineTypeModelAndSerialNumber machineTypeModelAndSerialNumber;

    public MachineTypeModelAndSerialNumber getMachineTypeModelAndSerialNumber() {
        return machineTypeModelAndSerialNumber;
    }


    @JsonProperty("ManagedSystems")
    protected List<Link> associatedManagedSystems;

    public List<Link> getAssociatedManagedSystems() {
        // TODO: Security - Return new array, so receiver cannot modify ours.
        return new ArrayList<>(associatedManagedSystems);
    }


    @JsonProperty("ManagementConsoleName")
    public String managementConsoleName;

    @Override
    public String getName() {
        return managementConsoleName.replace("\n", "").trim();
    }


    @JsonProperty("VersionInfo")
    public VersionInfo versionInfo;

    @JsonProperty("BIOS")
    protected String bios;

    @JsonProperty("BaseVersion")
    protected String baseVersion;

    public String getBaseVersion() {
        return baseVersion;
    }

    @JsonProperty("IFixDetails")
    public IFixDetails iFixDetails;


    @JsonIgnoreProperties({ "ksv", "kxe", "kb", "schemaVersion", "Metadata" })
    static class IFixDetails {
        @JsonProperty("IFixDetail")
        public List<IFixDetail> iFixDetailList;
    }

    @JsonProperty("ProcConfiguration")
    public ProcConfiguration procConfiguration;

    @JsonIgnoreProperties({ "ksv", "kxe", "kb", "schemaVersion", "Metadata", "Atom" })
    static class ProcConfiguration {
        @JsonProperty("NumberOfProcessors")
        public Integer numberOfProcessors;

        @JsonProperty("ModelName")
        public String modelName;

        @JsonProperty("Architecture")
        public String architecture;
    }

    @JsonProperty("MemConfiguration")
    public MemConfiguration memConfiguration;

    @JsonIgnoreProperties({ "ksv", "kxe", "kb", "schemaVersion", "Metadata", "Atom" })
    static class MemConfiguration {

        @JsonProperty("TotalMemory")
        public Integer totalMemory;

        @JsonProperty("TotalSwapMemory")
        public Integer totalSwapMemory;
    }

}
