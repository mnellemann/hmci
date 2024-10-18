package biz.nellemann.hmci;

public class MeasurementItem {

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

}
