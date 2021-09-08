package biz.nellemann.hmci.pcm;

import com.serjltt.moshi.adapters.FirstElement;


public final class GenericVirtualAdapter {

    public String id = "";
    public String type = "";
    public Integer viosId = 0;
    public String physicalLocation = "";

    @FirstElement
    public Number numOfReads = 0.0;

    @FirstElement
    public Number numOfWrites = 0.0;

    @FirstElement
    public Number readBytes = 0.0;

    @FirstElement
    public Number writeBytes = 0.0;

    @FirstElement
    public Number transmittedBytes = 0.0;

}
