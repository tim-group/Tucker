package com.timgroup.tucker.info.component;

import com.timgroup.tucker.info.Status;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeFalse;

public class JarVersionComponentTest {
    
    @Test
    public void defaultIdAndLabelAreSuitable() throws Exception {
        assertEquals("version", new JarVersionComponent(String.class).getId());
        assertEquals("Version", new JarVersionComponent(String.class).getLabel());
    }
    
    @Test
    public void reportStatusIsInfo() throws Exception {
        assertEquals(Status.INFO, new JarVersionComponent(String.class).getReport().getStatus());
    }
    
    @Test
    public void reportValueIsImplementationVersionOfPackageContainingAnchorClass() throws Exception {
        String jdkVersion = System.getProperty("java.version");
        assumeFalse("Doesn't work like this on Java 9", jdkVersion.startsWith("9.") || jdkVersion.startsWith("10") || jdkVersion.startsWith("11") || jdkVersion.startsWith("12")|| jdkVersion.startsWith("13"));
        assertEquals(jdkVersion, new JarVersionComponent(String.class).getReport().getValue());
    }
    
}
