package com.timgroup.status;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StatusTest {
    
    @Test
    public void statusesCompareCorrectly() throws Exception {
        assertLessThan(Status.ERROR, Status.WARN);
        assertLessThan(Status.WARN, Status.OK);
        assertLessThan(Status.OK, Status.INFO);
    }
    
    private void assertLessThan(Status a, Status b) {
        assertTrue(a.compareTo(b) < 0);
    }
    
    @Test
    public void statusesCombineCorrectly() throws Exception {
        assertEquals(Status.INFO, Status.INFO.or(Status.INFO));
        assertEquals(Status.OK, Status.INFO.or(Status.OK));
        assertEquals(Status.WARN, Status.INFO.or(Status.WARN));
        assertEquals(Status.ERROR, Status.INFO.or(Status.ERROR));
        
        assertEquals(Status.OK, Status.OK.or(Status.INFO));
        assertEquals(Status.OK, Status.OK.or(Status.OK));
        assertEquals(Status.WARN, Status.OK.or(Status.WARN));
        assertEquals(Status.ERROR, Status.OK.or(Status.ERROR));
        
        assertEquals(Status.WARN, Status.WARN.or(Status.INFO));
        assertEquals(Status.WARN, Status.WARN.or(Status.OK));
        assertEquals(Status.WARN, Status.WARN.or(Status.WARN));
        assertEquals(Status.ERROR, Status.WARN.or(Status.ERROR));
        
        assertEquals(Status.ERROR, Status.ERROR.or(Status.INFO));
        assertEquals(Status.ERROR, Status.ERROR.or(Status.OK));
        assertEquals(Status.ERROR, Status.ERROR.or(Status.INFO));
        assertEquals(Status.ERROR, Status.ERROR.or(Status.ERROR));
    }
    
}
