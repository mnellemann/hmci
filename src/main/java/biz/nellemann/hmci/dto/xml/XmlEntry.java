package biz.nellemann.hmci.dto.xml;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

//@JsonIgnoreProperties({ "author", "etag" })
@JsonIgnoreProperties(ignoreUnknown = true)
public class XmlEntry implements Serializable {

    private static final long serialVersionUID = 1L;

    public String id;              // 2c6b6620-e3e3-3294-aaf5-38e546ff672b
    public String title;           // ManagementConsole
    public String published;       // 2021-11-09T21:13:40.467+01:00

    public Category category;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public class Category {
        public String term;
    }

    @JsonProperty("link")
    public Link link;

    //public List<Link> links;
    /*public List<Link> getLinks() {
        return links;
    }
     */

    public Content content;

    public Content getContent() {
        return content;
    }

    public boolean hasContent() {
        return content != null;
    }

    @JsonIgnoreProperties({ "type" })
    public static class Content {

        @JsonProperty("ManagementConsole")
        private ManagementConsoleEntry managementConsoleEntry;

        public ManagementConsoleEntry getManagementConsole() {
            return managementConsoleEntry;
        }

        public boolean isManagementConsole() {
            return managementConsoleEntry != null;
        }

        @JsonProperty("ManagedSystem")
        private ManagedSystemEntry managedSystemEntry;

        public ManagedSystemEntry getManagedSystemEntry() {
            return managedSystemEntry;
        }

        public boolean isManagedSystem() {
            return managedSystemEntry != null;
        }


        @JsonAlias("VirtualIOServer")
        private VirtualIOServerEntry virtualIOServerEntry;

        public VirtualIOServerEntry getVirtualIOServerEntry() {
            return virtualIOServerEntry;
        }

        public boolean isVirtualIOServer() {
            return virtualIOServerEntry != null;
        }


        @JsonAlias("LogicalPartition")
        private LogicalPartitionEntry logicalPartitionEntry;

        public LogicalPartitionEntry getLogicalPartitionEntry() {
            return logicalPartitionEntry;
        }

        public boolean isLogicalPartition() {
            return logicalPartitionEntry != null;
        }

    }

}
