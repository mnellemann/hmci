package biz.nellemann.hmci.dto.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class SystemUtil {

    @JsonProperty("utilInfo")
    public UtilInfo utilInfo;

    public UtilInfo getUtilInfo() {
        return utilInfo;
    }

    @JsonUnwrapped
    @JsonProperty("utilSamples")
    public List<UtilSample> samples;

    public UtilSample getSample(int n) {
        return samples.size() > n ? samples.get(n) : new UtilSample();
    }

    public UtilSample getSample() {
        return samples.size() > 0 ? samples.get(0) : new UtilSample();
    }

}
