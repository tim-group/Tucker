package com.timgroup.tucker.info.component;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.regex.Pattern;

import org.junit.Test;

import com.timgroup.tucker.info.Status;

public final class MemoryUsageComponentTest {

    @Test
    public void defaultIdAndLabelAreSuitable() throws Exception {
        assertEquals("memoryUsage", new MemoryUsageComponent().getId());
        assertEquals("JVM Memory Usage", new MemoryUsageComponent().getLabel());
    }

    @Test
    public void reportStatusReflectsUsage() throws Exception {
        assertEquals(Status.OK, new MemoryUsageComponent().getReport().getStatus());
        assertEquals(Status.WARNING, new MemoryUsageComponent(0, 100).getReport().getStatus());
        assertEquals(Status.CRITICAL, new MemoryUsageComponent(0, 0).getReport().getStatus());
    }

    @Test
    public void reportValueIsAFormattedPercentageString() throws Exception {
        final Pattern outputFormat = Pattern.compile("\\d\\d?%");
        assertTrue(outputFormat.matcher((String)new MemoryUsageComponent().getReport().getValue()).matches());
    }

}
