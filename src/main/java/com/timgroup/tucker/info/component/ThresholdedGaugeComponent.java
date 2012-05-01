package com.timgroup.tucker.info.component;

import com.timgroup.tucker.info.Status;
import com.yammer.metrics.core.Gauge;

public class ThresholdedGaugeComponent<T extends Comparable<T>> extends GaugeComponent<T> {
    
    private final T warningThreshold;
    private final T criticalThreshold;
    
    public ThresholdedGaugeComponent(String id, String label, Gauge<T> gauge, T warningThreshold, T criticalThreshold) {
        super(id, label, gauge);
        this.warningThreshold = warningThreshold;
        this.criticalThreshold = criticalThreshold;
    }
    
    @Override
    protected Status getStatus(T value) {
        if (value.compareTo(criticalThreshold) > 0) {
            return Status.CRITICAL;
        }
        if (value.compareTo(warningThreshold) > 0) {
            return Status.WARNING;
        }
        return Status.OK;
    }
    
}
