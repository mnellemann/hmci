package biz.nellemann.hmci.dto.xml;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/*
@JsonIgnoreProperties({
    "ksv", "kxe", "kb", "schemaVersion", "Metadata", "AllowPerformanceDataCollection",
    "AssociatedPartitionProfile", "AvailabilityPriority", "CurrentProcessorCompatibilityMode", "CurrentProfileSync",
    "IsBootable", "IsConnectionMonitoringEnabled", "IsOperationInProgress", "IsRedundantErrorPathReportingEnabled",
    "IsTimeReferencePartition", "IsVirtualServiceAttentionLEDOn", "IsVirtualTrustedPlatformModuleEnabled",
    "KeylockPosition", "LogicalSerialNumber", "OperatingSystemVersion", "PartitionCapabilities", "PartitionID",
    "PartitionIOConfiguration", "PartitionMemoryConfiguration", "PartitionProcessorConfiguration", "PartitionProfiles",
    "PendingProcessorCompatibilityMode", "ProcessorPool", "ProgressPartitionDataRemaining", "ProgressPartitionDataTotal",
    "ProgressState", "ResourceMonitoringControlState", "ResourceMonitoringIPAddress", "AssociatedManagedSystem",
    "ClientNetworkAdapters", "HostEthernetAdapterLogicalPorts", "MACAddressPrefix", "IsServicePartition",
    "PowerVMManagementCapable", "ReferenceCode", "AssignAllResources", "HardwareAcceleratorQoS", "LastActivatedProfile",
    "HasPhysicalIO", "AllowPerformanceDataCollection", "PendingSecureBoot", "CurrentSecureBoot", "BootMode",
    "PowerOnWithHypervisor", "Description", "MigrationStorageViosDataStatus", "MigrationStorageViosDataTimestamp",
    "RemoteRestartCapable", "SimplifiedRemoteRestartCapable", "HasDedicatedProcessorsForMigration", "SuspendCapable",
    "MigrationDisable", "MigrationState", "RemoteRestartState", "VirtualFibreChannelClientAdapters",
    "VirtualSCSIClientAdapters", "BootListInformation"
})
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class LogicalPartitionEntry implements Serializable, ResourceEntry {

    private static final long serialVersionUID = 1L;

    @JsonProperty("PartitionName")
    public String partitionName;

    @JsonProperty("PartitionState")
    public String partitionState;

    @JsonProperty("PartitionType")
    public String partitionType;

    @JsonProperty("PartitionUUID")
    public String partitionUUID;

    @JsonProperty("OperatingSystemType")
    public String operatingSystemType;

    @Override
    public String getName() {
        return partitionName.trim();
    }
}

