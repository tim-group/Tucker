package com.timgroup.status;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class ReportTest {
    
    @Test
    public void worstStatusCanBeFound() throws Exception {
        assertEquals(Status.OK, Report.worstStatus(Collections.<Report> emptySet()));
        
        assertEquals(Status.OK, Report.worstStatus(Collections.singleton(new Report(Status.INFO, null))));
        assertEquals(Status.OK, Report.worstStatus(Collections.singleton(new Report(Status.OK, null))));
        assertEquals(Status.ERROR, Report.worstStatus(Collections.singleton(new Report(Status.ERROR, null))));
        
        assertEquals(Status.OK, Report.worstStatus(Arrays.asList(new Report(Status.INFO, null), new Report(Status.INFO, null))));
        assertEquals(Status.OK, Report.worstStatus(Arrays.asList(new Report(Status.INFO, null), new Report(Status.OK, null))));
        assertEquals(Status.ERROR, Report.worstStatus(Arrays.asList(new Report(Status.OK, null), new Report(Status.ERROR, null))));
    }
    
    @Test
    public void reportMightNotHaveAValue() throws Exception {
        assertTrue(new Report(Status.INFO, "foo").hasValue());
        assertFalse(new Report(Status.INFO).hasValue());
    }
    
    @Test
    public void reportMightFail() throws Exception {
        assertTrue(new Report(Status.INFO, "foo").isSuccessful());
        assertTrue(new Report(Status.INFO).isSuccessful());
        
        assertFalse(new Report(Status.INFO, new Error()).isSuccessful());
        
        assertFalse(new Report(new Error()).isSuccessful());
        assertEquals(Status.ERROR, new Report(new Error()).getStatus());
    }
    
}
