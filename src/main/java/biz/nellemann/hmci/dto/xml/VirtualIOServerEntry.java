package biz.nellemann.hmci.dto.xml;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VirtualIOServerEntry implements Serializable, ResourceEntry {

    private static final long serialVersionUID = 1L;

    @JsonProperty("PartitionName")
    private String partitionName;

    public String getPartitionName() {
        return partitionName;
    }

    @Override
    public String getName() {
        return partitionName;
    }

}

