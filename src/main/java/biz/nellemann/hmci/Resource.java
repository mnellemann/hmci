package biz.nellemann.hmci;

import biz.nellemann.hmci.dto.json.ProcessedMetrics;
import biz.nellemann.hmci.dto.json.SystemUtil;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Resource {

    private final static Logger log = LoggerFactory.getLogger(Resource.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    protected SystemUtil metric;


    Resource() {
        objectMapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        objectMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
    }


    void deserialize(String json) {
        if(json == null || json.length() < 1) {
            return;
        }

        try {
            ProcessedMetrics processedMetrics = objectMapper.readValue(json, ProcessedMetrics.class);
            metric = processedMetrics.systemUtil;
        } catch (Exception e) {
            log.error("deserialize() - error: {}", e.getMessage());
        }
    }


    Instant getTimestamp() {
        Instant instant = Instant.now();

        if (metric == null) {
            return instant;
        }

        String timestamp = metric.getSample().sampleInfo.timestamp;
        try {
            log.trace("getTimeStamp() - PMC Timestamp: {}", timestamp);
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[XXX][X]");
            instant = Instant.from(dateTimeFormatter.parse(timestamp));
            log.trace("getTimestamp() - Instant: {}", instant.toString());
        } catch(DateTimeParseException e) {
            log.warn("getTimestamp() - parse error: {}", timestamp);
        }

        return instant;
    }

}
