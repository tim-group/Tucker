package com.timgroup.status;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class VersionTest {
    
    @Test
    public void defaultIdAndLabelAreSuitable() throws Exception {
        assertEquals("version", new Version(String.class).getId());
        assertEquals("Version", new Version(String.class).getLabel());
    }
    
    @Test
    public void reportStatusIsInfo() throws Exception {
        assertEquals(Status.INFO, new Version(String.class).getReport().getStatus());
    }
    
    @Test
    public void reportValueIsImplementationVersionOfPackageContainingAnchorClass() throws Exception {
        String jdkVersion = System.getProperty("java.version");
        assertEquals(jdkVersion, new Version(String.class).getReport().getValue());
    }
    
}
