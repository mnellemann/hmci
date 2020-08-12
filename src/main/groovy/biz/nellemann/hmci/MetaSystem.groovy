package biz.nellemann.hmci

import biz.nellemann.hmci.pojo.PcmData
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j

import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@Slf4j
class MetaSystem {

    protected PcmData metrics

    void processMetrics(String json) {
        def pcmMap = new JsonSlurper().parseText(json)
        metrics = new PcmData(pcmMap as Map)
    }


    Instant getTimestamp() {

        String timeStamp = metrics.systemUtil.utilSamples.first().sampleInfo.timeStamp
        Instant instant
        try {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[XXX][X]");
            instant = Instant.from(dateTimeFormatter.parse(timeStamp))
        } catch(DateTimeParseException e) {
            log.warn("getTimestamp() - parse error: " + timeStamp)
        }
        return instant
    }

}
