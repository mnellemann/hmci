package biz.nellemann.hmci;

import biz.nellemann.hmci.dto.json.ProcessedMetrics;
import biz.nellemann.hmci.dto.json.SystemUtil;
import biz.nellemann.hmci.dto.json.UtilSample;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

public abstract class Resource {

    private final static Logger log = LoggerFactory.getLogger(Resource.class);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ArrayList<String> sampleHistory = new ArrayList<>();

    protected SystemUtil metric;
    protected final int maxNumberOfSamples = 60;
    protected final int minNumberOfSamples = 5;
    protected int noOfSamples = maxNumberOfSamples;



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
            log.trace("deserialize() - samples: {}", metric.samples.size());
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


    Instant getTimestamp(int sampleNumber) {
        Instant instant = Instant.now();

        if (metric == null) {
            return instant;
        }

        String timestamp = metric.getSample(sampleNumber).sampleInfo.timestamp;
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


    public void process() {

        if(metric == null) {
            return;
        }

        int processed = 0;
        int sampleSize = metric.samples.size();
        log.debug("process() - Samples Returned: {}, Samples in History: {}, Fetch Next Counter: {}", sampleSize, sampleHistory.size(), noOfSamples);
        for(int i = 0; i<sampleSize; i++) {
            UtilSample sample = metric.getSample(i);
            String timestamp = sample.getInfo().timestamp;

            if(sampleHistory.contains(timestamp)) {
                //log.info("process() - Sample \"{}\" already processed", timestamp);
                continue;   // Already processed
            }

            try {
                process(i);
                processed++;
                sampleHistory.add(timestamp); // Add to processed history
            } catch (NullPointerException e) {
                log.warn("process() - error", e);
            }
        }

        // Remove old elements from history
        for(int n = noOfSamples; n < sampleHistory.size(); n++) {
            //log.info("process() - Removing element no. {} from sampleHistory: {}", n, sampleHistory.get(0));
            sampleHistory.remove(0);
        }

        // Decrease down to minSamples
        if(noOfSamples > minNumberOfSamples) {
            noOfSamples = Math.min( (noOfSamples - 1), Math.max( (noOfSamples - processed) + 5, minNumberOfSamples));
        }

    }

    public abstract void process(int sample) throws NullPointerException;

}
