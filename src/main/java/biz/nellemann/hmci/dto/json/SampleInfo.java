package biz.nellemann.hmci.dto.json;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class SampleInfo {

    @JsonProperty("timeStamp")
    public String timestamp;
    public String getTimeStamp() {
        return timestamp;
    }

    public Integer status;

    @JsonProperty("errorInfo")
    public List<ErrorInfo> errors;

    static class ErrorInfo {
        public String errId;
        public String errMsg;
        public String uuid;
        public String reportedBy;
        public Integer occurrenceCount;
    }

}
