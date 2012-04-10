package com.timgroup.status;

import com.yammer.metrics.core.Gauge;

public class ThresholdedGaugeComponent<T extends Comparable<T>> extends GaugeComponent<T> {
    
    private final T warnThreshold;
    private final T errorThreshold;
    
    public ThresholdedGaugeComponent(String id, String label, Gauge<T> gauge, T warnThreshold, T errorThreshold) {
        super(id, label, gauge);
        this.warnThreshold = warnThreshold;
        this.errorThreshold = errorThreshold;
    }
    
    @Override
    protected Status getStatus(T value) {
        if (value.compareTo(errorThreshold) > 0) {
            return Status.ERROR;
        } else if (value.compareTo(warnThreshold) > 0) {
            return Status.WARN;
        } else {
            return Status.OK;
        }
    }
    
}
