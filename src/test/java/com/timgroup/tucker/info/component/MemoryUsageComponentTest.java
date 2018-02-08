package com.timgroup.tucker.info.component;

import org.junit.Test;

import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public final class MemoryUsageComponentTest {

    @Test
    public void defaultIdAndLabelAreSuitable() throws Exception {
        assertEquals("memoryUsage", new MemoryUsageComponent().getId());
        assertEquals("JVM Memory Usage", new MemoryUsageComponent().getLabel());
    }

    @Test
    public void reportValueIsAFormattedPercentageString() throws Exception {
        final Pattern outputFormat = Pattern.compile("\\d\\d?%");
        assertTrue(outputFormat.matcher((String)new MemoryUsageComponent().getReport().getValue()).matches());
    }

}
