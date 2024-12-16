package biz.nellemann.hmci.dto.xml;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

import java.io.Serializable;
import java.util.List;

//@JsonIgnoreProperties({ "link" })
@JsonIgnoreProperties(ignoreUnknown = true)
public class XmlFeed implements Serializable {

    private static final long serialVersionUID = 1L;

    public String id;                      // 347ecfcf-acac-3724-8915-a3d7d7a6f298
    public String updated;                 // 2021-11-09T21:13:39.591+01:00
    public String generator;               // IBM Power Systems Management Console

    @JsonProperty("link")
    @JacksonXmlElementWrapper(useWrapping = false)
    public List<Link> links;                // <link rel="SELF" href="https://10.32.64.39:12443/rest/api/uom/ManagementConsole"/>

    @JsonProperty("entry")
    @JacksonXmlElementWrapper(useWrapping = false)
    public List<XmlEntry> entries;

    public XmlEntry getEntry() {
        return !entries.isEmpty() ? entries.get(0) : null;
    }

}
