package biz.nellemann.hmci;

import java.util.Map;

public class Measurement {

    final Map<String, String> tags;
    final Map<String, Number> fields;

    Measurement(Map<String, String> tags, Map<String, Number> fields) {
        this.tags = tags;
        this.fields = fields;
    }

}
