package biz.nellemann.hmci.dto.xml;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Link implements Serializable {

    private static final long serialVersionUID = 1L;

    @JacksonXmlProperty(isAttribute = true)
    public String rel;

    public String getRel() {
        return rel;
    }

    @JacksonXmlProperty(isAttribute = true)
    public String type;

    public String getType() {
        return type;
    }

    @JacksonXmlProperty(isAttribute = true)
    public String href;

    public String getHref() {
        return href;
    }

}
