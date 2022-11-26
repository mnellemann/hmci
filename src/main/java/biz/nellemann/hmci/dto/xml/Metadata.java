package biz.nellemann.hmci.dto.xml;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Metadata {

    @JsonProperty("Atom")
    public Atom atom;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public class Atom {

        @JsonProperty("AtomID")
        public String atomID;

        @JsonProperty("AtomCreated")
        public String atomCreated;
    }
}
