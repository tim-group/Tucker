package com.timgroup.tucker.info.component;

import com.timgroup.tucker.info.Component;
import com.timgroup.tucker.info.Report;
import com.timgroup.tucker.info.Status;
import com.yammer.metrics.core.Gauge;

public class GaugeComponent<T> extends Component {
    
    private final Gauge<T> gauge;
    
    public GaugeComponent(String id, String label, Gauge<T> gauge) {
        super(id, label);
        this.gauge = gauge;
    }
    
    @Override
    public Report getReport() {
        T value = gauge.value();
        return new Report(getStatus(value), value);
    }
    
    protected Status getStatus(T value) {
        return Status.INFO;
    }
    
}
