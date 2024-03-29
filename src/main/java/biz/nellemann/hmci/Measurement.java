/*
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
package biz.nellemann.hmci;

import java.time.Instant;
import java.util.Map;

public class Measurement {

    final Instant timestamp;
    final Map<String, String> tags;
    final Map<String, Object> fields;

    Measurement(Map<String, String> tags, Map<String, Object> fields) {
        this.timestamp = Instant.now();
        this.tags = tags;
        this.fields = fields;
    }

    Measurement(Instant timestamp, Map<String, String> tags, Map<String, Object> fields) {
        this.timestamp = timestamp;
        this.tags = tags;
        this.fields = fields;
    }

}
