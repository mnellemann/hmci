package biz.nellemann.hmci.pcm;

import com.serjltt.moshi.adapters.FirstElement;

public class Temperature {

    public String entityId = "";
    public String entityInstance = "";

    @FirstElement
    public Float temperatureReading = 0.0f;

}
