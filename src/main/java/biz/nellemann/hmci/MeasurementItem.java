package biz.nellemann.hmci;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MeasurementItem {

    private final static Logger log = LoggerFactory.getLogger(MeasurementItem.class);

    MeasurementType type = MeasurementType.COUNTER;
    MeasurementUnit unit = MeasurementUnit.UNITS;

    String key;
    Object value;

    String description;

    MeasurementItem(MeasurementType type, MeasurementUnit unit, String key, Object value, String description) {
        this.type = type;
        this.unit = unit;
        this.key = key;
        this.value = value;
        this.description = description;
    }

    MeasurementItem(MeasurementType type, MeasurementUnit unit, String key, Object value) {
        this.type = type;
        this.unit = unit;
        this.key = key;
        this.value = value;
    }

    MeasurementItem(MeasurementType type, String key, Object value, String description) {
        this.type = type;
        this.key = key;
        this.value = value;
        this.description = description;
    }

    MeasurementItem(MeasurementType type, String key, Object value) {
        this.type = type;
        this.key = key;
        this.value = value;
    }

    MeasurementItem(String key, Object value, String description) {
        this.key = key;
        this.value = value;
        this.description = description;
    }

    MeasurementItem(String key, Object value) {
        this.key = key;
        this.value = value;
    }

    public void setMeasurementType(MeasurementType type) {
        this.type = type;
    }

    public void setMeasurementUnit(MeasurementUnit unit) {
        this.unit = unit;
    }

    public MeasurementType getMeasurementType() {
        return type;
    }

    public MeasurementUnit getMeasurementUnit() {
        return unit;
    }

    public String getDescription() {
        return description;
    }


    public double getDoubleValue() {
        double d = 0;
        try {
            d = (double) value;
        } catch(ClassCastException e) {
            log.warn("getDoubleValue() - not double? {} => {}", key, value);
        }
        return d;
    }


    public long getLongValue() {
        long l = 0;
        try {
            l = (long) value;
        } catch (ClassCastException e) {
            log.warn("getLongValue() - not long? {} => {}", key, value);
        }
        return l;
    }


    public String getStringValue() {
        String s = null;
        try {
            s = String.valueOf(value);
        } catch (ClassCastException e) {
            log.warn("getStringValue() - not String? {} => {}", key, value);
        }
        return s;
    }

}
