package com.timgroup.status;

import com.yammer.metrics.core.Gauge;

public class GaugeComponent<T> extends Component {
    
    private final Gauge<T> gauge;
    
    public GaugeComponent(String id, String label, Gauge<T> gauge) {
        super(id, label);
        this.gauge = gauge;
    }
    
    @Override
    public Report getReport() {
        return new Report(Status.INFO, gauge.value());
    }
    
}
