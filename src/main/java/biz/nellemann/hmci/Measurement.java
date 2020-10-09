package biz.nellemann.hmci;

import java.util.Map;

public class Measurement {

    Map<String, String> tags;
    Map<String, Number> fields;

    Measurement() {
    }

    Measurement(Map<String, String> tags, Map<String, Number> fields) {
        this.tags = tags;
        this.fields = fields;
    }
}
