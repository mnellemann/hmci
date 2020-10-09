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
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@Slf4j
@CompileStatic
abstract class MetaSystem {

    private final Moshi moshi;
    private final JsonAdapter<PcmData> jsonAdapter;

    protected PcmData metrics

    MetaSystem() {
        try {
            moshi = new Moshi.Builder().add(new NumberAdapter()).add(new BigDecimalAdapter())build();
            jsonAdapter = moshi.adapter(PcmData.class);
        } catch(Exception e) {
            log.warn("MetaSystem() error", e)
            throw new ExceptionInInitializerError(e);
        }
    }

    //@CompileDynamic
    void processMetrics(String json) {

        try {
            metrics = jsonAdapter.fromJson(json);
        } catch(Exception e) {
            log.warn("processMetrics() error", e)
        }

        //Map pcmMap = new JsonSlurper().parseText(json) as Map
        //metrics = new PcmData(pcmMap)
    }

    //@CompileDynamic
    Instant getTimestamp()  {

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


    class BigDecimalAdapter {

        @FromJson
        BigDecimal fromJson(String string) {
            return new BigDecimal(string);
        }

        @ToJson
        String toJson(BigDecimal value) {
            return value.toString();
        }
    }

    class NumberAdapter {

        @FromJson
        Number fromJson(String string) {
            return new Double(string);
        }

        @ToJson
        String toJson(Number value) {
            return value.toString();
        }
    }

}

