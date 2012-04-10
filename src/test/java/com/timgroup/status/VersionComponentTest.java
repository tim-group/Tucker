package com.timgroup.status;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class VersionComponentTest {
    
    @Test
    public void defaultIdAndLabelAreSuitable() throws Exception {
        assertEquals("version", new VersionComponent(String.class).getId());
        assertEquals("Version", new VersionComponent(String.class).getLabel());
    }
    
    @Test
    public void reportStatusIsInfo() throws Exception {
        assertEquals(Status.INFO, new VersionComponent(String.class).getReport().getStatus());
    }
    
    @Test
    public void reportValueIsImplementationVersionOfPackageContainingAnchorClass() throws Exception {
        String jdkVersion = System.getProperty("java.version");
        assertEquals(jdkVersion, new VersionComponent(String.class).getReport().getValue());
    }
    
}
