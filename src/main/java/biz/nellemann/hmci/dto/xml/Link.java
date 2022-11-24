package biz.nellemann.hmci.dto.xml;


import java.io.Serializable;

public class Link implements Serializable {

    private static final long serialVersionUID = 1L;

    public String rel;

    public String getRel() {
        return rel;
    }


    public String type;

    public String getType() {
        return type;
    }

    public String href;

    public String getHref() {
        return href;
    }

}
