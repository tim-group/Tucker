package com.timgroup.tucker.info.status;

import org.junit.Test;

import com.timgroup.tucker.info.Status;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StatusTest {
    
    @Test
    public void statusesCompareCorrectly() throws Exception {
        assertLessThan(Status.CRITICAL, Status.WARNING);
        assertLessThan(Status.WARNING, Status.OK);
        assertLessThan(Status.OK, Status.INFO);
    }
    
    private void assertLessThan(Status a, Status b) {
        assertTrue(a.compareTo(b) < 0);
    }
    
    @Test
    public void statusesCombineCorrectly() throws Exception {
        assertEquals(Status.INFO, Status.INFO.or(Status.INFO));
        assertEquals(Status.OK, Status.INFO.or(Status.OK));
        assertEquals(Status.WARNING, Status.INFO.or(Status.WARNING));
        assertEquals(Status.CRITICAL, Status.INFO.or(Status.CRITICAL));
        
        assertEquals(Status.OK, Status.OK.or(Status.INFO));
        assertEquals(Status.OK, Status.OK.or(Status.OK));
        assertEquals(Status.WARNING, Status.OK.or(Status.WARNING));
        assertEquals(Status.CRITICAL, Status.OK.or(Status.CRITICAL));
        
        assertEquals(Status.WARNING, Status.WARNING.or(Status.INFO));
        assertEquals(Status.WARNING, Status.WARNING.or(Status.OK));
        assertEquals(Status.WARNING, Status.WARNING.or(Status.WARNING));
        assertEquals(Status.CRITICAL, Status.WARNING.or(Status.CRITICAL));
        
        assertEquals(Status.CRITICAL, Status.CRITICAL.or(Status.INFO));
        assertEquals(Status.CRITICAL, Status.CRITICAL.or(Status.OK));
        assertEquals(Status.CRITICAL, Status.CRITICAL.or(Status.INFO));
        assertEquals(Status.CRITICAL, Status.CRITICAL.or(Status.CRITICAL));
    }
    
}
