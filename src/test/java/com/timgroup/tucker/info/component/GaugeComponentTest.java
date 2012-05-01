package com.timgroup.tucker.info.component;

import org.junit.Test;

import com.timgroup.tucker.info.Status;
import com.timgroup.tucker.info.component.GaugeComponent;
import com.yammer.metrics.core.Gauge;

import static org.junit.Assert.assertEquals;

public class GaugeComponentTest {
    
    @Test
    public void reportsValueOfGauge() throws Exception {
        GaugeComponent<Integer> gaugeComponent = new GaugeComponent<Integer>("", "", new Gauge<Integer>() {
            @Override
            public Integer value() {
                return 23;
            }
        });
        
        assertEquals(23, gaugeComponent.getReport().getValue());
    }
    
    @Test
    public void reportsInfoStatus() throws Exception {
        GaugeComponent<Integer> gaugeComponent = new GaugeComponent<Integer>("", "", new Gauge<Integer>() {
            @Override
            public Integer value() {
                return 23;
            }
        });
        
        assertEquals(Status.INFO, gaugeComponent.getReport().getStatus());
    }
    
}
