package biz.nellemann.hmci.dto.xml;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/*
@JsonIgnoreProperties({
    "schemaVersion", "Metadata", "AssociatedIPLConfiguration", "AssociatedSystemCapabilities",
    "AssociatedSystemIOConfiguration", "AssociatedSystemMemoryConfiguration", "AssociatedSystemProcessorConfiguration",
    "AssociatedSystemSecurity", "DetailedState", "ManufacturingDefaultConfigurationEnabled", "MaximumPartitions",
    "MaximumPowerControlPartitions", "MaximumRemoteRestartPartitions", "MaximumSharedProcessorCapablePartitionID",
    "MaximumSuspendablePartitions", "MaximumBackingDevicesPerVNIC", "PhysicalSystemAttentionLEDState",
    "PrimaryIPAddress", "ServiceProcessorFailoverEnabled", "ServiceProcessorFailoverReason", "ServiceProcessorFailoverState",
    "ServiceProcessorVersion", "VirtualSystemAttentionLEDState", "SystemMigrationInformation", "ReferenceCode",
    "MergedReferenceCode", "EnergyManagementConfiguration", "IsPowerVMManagementMaster", "IsClassicHMCManagement",
    "IsPowerVMManagementWithoutMaster", "IsManagementPartitionPowerVMManagementMaster", "IsHMCPowerVMManagementMaster",
    "IsNotPowerVMManagementMaster", "IsPowerVMManagementNormalMaster", "IsPowerVMManagementPersistentMaster",
    "IsPowerVMManagementTemporaryMaster", "IsPowerVMManagementPartitionEnabled", "SupportedHardwareAcceleratorTypes",
    "CurrentStealableProcUnits", "CurrentStealableMemory", "Description", "SystemLocation", "SystemType",
    "ProcessorThrottling", "AssociatedPersistentMemoryConfiguration"
})*/
@JsonIgnoreProperties(ignoreUnknown = true)
public class ManagedSystemEntry implements Serializable, ResourceEntry {

    private static final long serialVersionUID = 1L;

    @JsonProperty("State")
    public String state;

    @JsonProperty("Hostname")
    public String hostname;

    //@JsonAlias("ActivatedLevel")
    @JsonProperty("ActivatedLevel")
    public Integer activatedLevel;

    public Integer getActivatedLevel() {
        return activatedLevel;
    }

    @JsonAlias("ActivatedServicePackNameAndLevel")
    public String activatedServicePackNameAndLevel;

    public String getActivatedServicePackNameAndLevel() {
        return activatedServicePackNameAndLevel;
    }

    @JsonAlias("SystemName")
    public String systemName = "";

    public String getSystemName() {
        return systemName.trim();
    }

    @Override
    public String getName() {
        return systemName.trim();
    }

    @JsonProperty("SystemTime")
    public Long systemTime;

    @JsonProperty("SystemFirmware")
    public String systemFirmware;

    @JsonAlias("AssociatedLogicalPartitions")
    public List<Link> associatedLogicalPartitions;

    public List<Link> getAssociatedLogicalPartitions() {
        return associatedLogicalPartitions != null ? associatedLogicalPartitions : new ArrayList<>();
    }


    @JsonAlias("AssociatedVirtualIOServers")
    public List<Link> associatedVirtualIOServers;

    public List<Link> getAssociatedVirtualIOServers() {
        return associatedVirtualIOServers != null ? associatedVirtualIOServers : new ArrayList<>();
    }


    @JsonAlias("MachineTypeModelAndSerialNumber")
    public MachineTypeModelAndSerialNumber machineTypeModelAndSerialNumber;

    public MachineTypeModelAndSerialNumber getMachineTypeModelAndSerialNumber() {
        return machineTypeModelAndSerialNumber;
    }

}
