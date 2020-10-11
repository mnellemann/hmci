package biz.nellemann.hmci.pcm;

import com.serjltt.moshi.adapters.FirstElement;
import com.squareup.moshi.Json;

public class SystemUtil {

    public UtilInfo utilInfo;

    @FirstElement
    @Json(name = "utilSamples")
    public UtilSample sample;

}
