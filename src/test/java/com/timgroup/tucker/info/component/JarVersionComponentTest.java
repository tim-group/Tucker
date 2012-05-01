package com.timgroup.tucker.info.component;

import org.junit.Test;

import com.timgroup.tucker.info.Status;
import com.timgroup.tucker.info.component.JarVersionComponent;

import static org.junit.Assert.assertEquals;

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
        assertEquals(jdkVersion, new JarVersionComponent(String.class).getReport().getValue());
    }
    
}
