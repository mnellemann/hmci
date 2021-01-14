package biz.nellemann.hmci.pcm;

import com.serjltt.moshi.adapters.FirstElement;


public class GenericVirtualAdapter {

    public String id = "";
    public String type = "";
    public Integer viosId = 0;
    public String physicalLocation = "";

    @FirstElement
    public Number numOfReads;

    @FirstElement
    public Number numOfWrites;

    @FirstElement
    public Number readBytes;

    @FirstElement
    public Number writeBytes;

    @FirstElement
    public Number transmittedBytes;

}
