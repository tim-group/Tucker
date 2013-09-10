package com.timgroup.tucker.info.component;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.timgroup.tucker.info.Report;
import com.timgroup.tucker.info.Status;

public final class SimpleValueComponentTest {
    private final SimpleValueComponent component = new SimpleValueComponent("myId", "myLabel");

    @Test
    public void reportsCorrectIdAndLabel() throws Exception {
        assertEquals("myId", component.getId());
        assertEquals("myLabel", component.getLabel());
    }

    @Test
    public void hasSensibleDefaultStatusAndValue() throws Exception {
        Report defaultReport = component.getReport();
        assertEquals(Status.INFO, defaultReport.getStatus());
        assertEquals("", defaultReport.getValue());
    }

    @Test
    public void reportsValueOfComponent() throws Exception {
        component.updateValue(Status.CRITICAL, "sausage");
        
        Report report = component.getReport();
        assertEquals(Status.CRITICAL, report.getStatus());
        assertEquals("sausage", report.getValue());
    }
}
