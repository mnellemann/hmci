package biz.nellemann.hmci.dto.xml;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties({ "Atom", "ksv", "kxe", "kb", "schemaVersion", "" })
public class IFixDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("IFix")
    public String iFix;

}
