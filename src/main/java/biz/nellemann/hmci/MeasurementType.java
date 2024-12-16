package biz.nellemann.hmci;

public enum MeasurementType {

    // A counter is a cumulative metric that represents a single monotonically increasing counter
    // whose value can only increase or be reset to zero on restart.
    COUNTER,

    // A gauge is a metric that represents a single numerical value that can arbitrarily go up and down.
    GAUGE,

    INFO
}
