package com.timgroup.status.component;

import org.junit.Test;

import com.timgroup.status.Status;
import com.timgroup.status.component.ThresholdedGaugeComponent;
import com.yammer.metrics.core.Gauge;

import static org.junit.Assert.assertEquals;

public class ThresholdedGaugeComponentTest {
    
    @Test
    public void reportsOkStatusIfValueLessThanWarningThreshold() throws Exception {
        ThresholdedGaugeComponent<Integer> gaugeComponent = new ThresholdedGaugeComponent<Integer>("", "", new Gauge<Integer>() {
            @Override
            public Integer value() {
                return 23;
            }
        }, 30, 40);
        
        assertEquals(Status.OK, gaugeComponent.getReport().getStatus());
    }
    
    @Test
    public void reportsWarningStatusIfValueGreaterThanWarningThresholdButLessThanCriticalThreshold() throws Exception {
        ThresholdedGaugeComponent<Integer> gaugeComponent = new ThresholdedGaugeComponent<Integer>("", "", new Gauge<Integer>() {
            @Override
            public Integer value() {
                return 23;
            }
        }, 20, 40);
        
        assertEquals(Status.WARNING, gaugeComponent.getReport().getStatus());
    }
    
    @Test
    public void reportsCriticalStatusIfValueGreaterThanCriticalThreshold() throws Exception {
        ThresholdedGaugeComponent<Integer> gaugeComponent = new ThresholdedGaugeComponent<Integer>("", "", new Gauge<Integer>() {
            @Override
            public Integer value() {
                return 23;
            }
        }, 10, 20);
        
        assertEquals(Status.CRITICAL, gaugeComponent.getReport().getStatus());
    }
    
}
