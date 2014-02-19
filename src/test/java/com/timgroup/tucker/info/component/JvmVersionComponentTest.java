package com.timgroup.tucker.info.component;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.timgroup.tucker.info.Status;

public final class JvmVersionComponentTest {
    
    @Test
    public void defaultIdAndLabelAreSuitable() throws Exception {
        assertEquals("jvmversion", new JvmVersionComponent().getId());
        assertEquals("JVM Version", new JvmVersionComponent().getLabel());
    }

    @Test
    public void reportStatusIsInfo() throws Exception {
        assertEquals(Status.INFO, new JvmVersionComponent().getReport().getStatus());
    }

    @Test
    public void reportValueIsImplementationVersionOfPackageContainingAnchorClass() throws Exception {
        String jdkVersion = System.getProperty("java.version");
        assertEquals(jdkVersion, new JvmVersionComponent().getReport().getValue());
    }

}
