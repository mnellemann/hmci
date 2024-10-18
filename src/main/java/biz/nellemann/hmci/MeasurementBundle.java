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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MeasurementBundle {

    final Instant timestamp;
    final String name;                  // InfluxDB Table - Promethus prepends to name
    final Map<String, String> tags;     // InfluxDB Tags - Prometheus Labels
    final Map<String, Object> fields;   // InfluxDB Fields
    final List<MeasurementItem> items;  // Promethus metrics

    MeasurementBundle(String name, Map<String, String> tags, Map<String, Object> fields) {
        this.timestamp = Instant.now();
        this.name = name;
        this.tags = tags;
        this.fields = fields;
        this.items = new ArrayList<>();
    }

    MeasurementBundle(String name, Map<String, String> tags, Map<String, Object> fields, List<MeasurementItem> items) {
        this.timestamp = Instant.now();
        this.name = name;
        this.tags = tags;
        this.fields = fields;
        this.items = items;
    }

    MeasurementBundle(Instant timestamp, String name, Map<String, String> tags, Map<String, Object> fields) {
        this.timestamp = timestamp;
        this.name = name;
        this.tags = tags;
        this.fields = fields;
        this.items = new ArrayList<>();
    }

    MeasurementBundle(Instant timestamp, String name, Map<String, String> tags, Map<String, Object> fields, List<MeasurementItem> items) {
        this.timestamp = timestamp;
        this.name = name;
        this.tags = tags;
        this.fields = fields;
        this.items = items;
    }

}
