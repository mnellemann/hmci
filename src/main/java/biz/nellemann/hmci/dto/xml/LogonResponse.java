package biz.nellemann.hmci.dto.xml;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties({ "schemaVersion", "Metadata" })
public class LogonResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("X-API-Session")
    private String token;

    public String getToken() {
        return token;
    }

}
