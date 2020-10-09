/**
 *    Copyright 2020 Mark Nellemann <mark.nellemann@gmail.com>
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package biz.nellemann.hmci

import biz.nellemann.hmci.pcm.PcmData
import groovy.json.JsonSlurper
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@Slf4j
@CompileStatic
abstract class MetaSystem {

    protected PcmData metrics

    @CompileDynamic
    void processMetrics(String json) {
        Map pcmMap = new JsonSlurper().parseText(json) as Map
        metrics = new PcmData(pcmMap)
    }

    @CompileDynamic
    Instant getTimestamp() {

        String timestamp = metrics.systemUtil.utilSamples.first().sampleInfo.timeStamp
        Instant instant = null
        try {
            log.debug("getTimeStamp() - PMC Timestamp: " + timestamp)
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[XXX][X]");
            instant = Instant.from(dateTimeFormatter.parse(timestamp))
            log.debug("getTimestamp() - Instant: " + instant.toString())
        } catch(DateTimeParseException e) {
            log.warn("getTimestamp() - parse error: " + timestamp)
        }

        return instant ?: Instant.now()
    }

}
