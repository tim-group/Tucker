package com.timgroup.status;

import com.yammer.metrics.core.Gauge;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ThresholdedGaugeComponentTest {
    
    @Test
    public void reportsOkStatusIfValueLessThanWarnThreshold() throws Exception {
        ThresholdedGaugeComponent<Integer> gaugeComponent = new ThresholdedGaugeComponent<Integer>("", "", new Gauge<Integer>() {
            
            @Override
            public Integer value() {
                return 23;
            }
            
        }, 30, 40);
        
        assertEquals(Status.OK, gaugeComponent.getReport().getStatus());
    }
    
    @Test
    public void reportsWarnStatusIfValueGreaterThanWarnThresholdButLessThanErrorThreshold() throws Exception {
        ThresholdedGaugeComponent<Integer> gaugeComponent = new ThresholdedGaugeComponent<Integer>("", "", new Gauge<Integer>() {
            
            @Override
            public Integer value() {
                return 23;
            }
            
        }, 20, 40);
        
        assertEquals(Status.WARN, gaugeComponent.getReport().getStatus());
    }
    
    @Test
    public void reportsErrorStatusIfValueGreaterThanErrorThreshold() throws Exception {
        ThresholdedGaugeComponent<Integer> gaugeComponent = new ThresholdedGaugeComponent<Integer>("", "", new Gauge<Integer>() {
            
            @Override
            public Integer value() {
                return 23;
            }
            
        }, 10, 20);
        
        assertEquals(Status.ERROR, gaugeComponent.getReport().getStatus());
    }
    
}
