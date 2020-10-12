package biz.nellemann.hmci.pcm;

import com.serjltt.moshi.adapters.FirstElement;

public class GenericPhysicalAdapters {

    public String id;
    public String type;
    public String physicalLocation;

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
