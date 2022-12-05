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
    protected final int maxNumberOfSamples = 120;
    protected final int minNumberOfSamples = 5;
    protected int currentNumberOfSamples = 15;



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

        int samples = metric.samples.size();
        //log.info("process() - Samples to process: {}, Samples in History: {}, Current Counter: {}", samples, sampleHistory.size(), currentNumberOfSamples);
        for(int i = 0; i<samples; i++) {
            UtilSample sample = metric.getSample(i);
            String timestamp = sample.getInfo().timestamp;

            if(sampleHistory.contains(timestamp)) {
                //log.info("process() - Sample \"{}\" already processed", timestamp);
                continue;   // Already processed
            }

            // Process
            //log.info("process() - Sample: {}", timestamp);
            process(i);

            // Add to end of history
            sampleHistory.add(timestamp);
        }

        // Remove old elements from history
        for(int n = currentNumberOfSamples; n < sampleHistory.size(); n++) {
            //log.info("process() - Removing element no. {} from sampleHistory: {}", n, sampleHistory.get(0));
            sampleHistory.remove(0);
        }

        // Slowly decrease until we reach minSamples
        if(currentNumberOfSamples > minNumberOfSamples) {
            currentNumberOfSamples--;
        }

    }

    public abstract void process(int sample);

}
