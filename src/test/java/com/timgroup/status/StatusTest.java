package com.timgroup.status;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StatusTest {
    
    @Test
    public void statusesCompareCorrectly() throws Exception {
        assertLessThan(Status.WARN, Status.OK);
        assertLessThan(Status.OK, Status.INFO);
    }
    
    private void assertLessThan(Status a, Status b) {
        Assert.assertTrue(a.compareTo(b) < 0);
    }
    
    @Test
    public void statusesCombineCorrectly() throws Exception {
        assertEquals(Status.INFO, Status.INFO.or(Status.INFO));
        assertEquals(Status.OK, Status.OK.or(Status.OK));
        assertEquals(Status.WARN, Status.WARN.or(Status.WARN));
        
        assertEquals(Status.OK, Status.INFO.or(Status.OK));
        assertEquals(Status.OK, Status.OK.or(Status.INFO));

        assertEquals(Status.WARN, Status.OK.or(Status.WARN));
        assertEquals(Status.WARN, Status.WARN.or(Status.OK));
        
        assertEquals(Status.WARN, Status.INFO.or(Status.WARN));
        assertEquals(Status.WARN, Status.WARN.or(Status.INFO));
    }
    
}
