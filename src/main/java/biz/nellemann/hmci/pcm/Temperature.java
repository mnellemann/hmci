package biz.nellemann.hmci.pcm;

import com.serjltt.moshi.adapters.FirstElement;

public final class Temperature {

    public String entityId = "";
    public String entityInstance = "";

    @FirstElement
    public Number temperatureReading = 0.0;

}
