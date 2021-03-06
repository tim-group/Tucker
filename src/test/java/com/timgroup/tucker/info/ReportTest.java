package com.timgroup.tucker.info;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ReportTest {
    
    @Test
    public void worstStatusCanBeFound() throws Exception {
        assertEquals(Status.OK, Report.worstStatus(Collections.<Report> emptySet()));
        
        assertEquals(Status.OK, Report.worstStatus(Collections.singleton(new Report(Status.INFO, null))));
        assertEquals(Status.OK, Report.worstStatus(Collections.singleton(new Report(Status.OK, null))));
        assertEquals(Status.CRITICAL, Report.worstStatus(Collections.singleton(new Report(Status.CRITICAL, null))));
        
        assertEquals(Status.OK, Report.worstStatus(Arrays.asList(new Report(Status.INFO, null), new Report(Status.INFO, null))));
        assertEquals(Status.OK, Report.worstStatus(Arrays.asList(new Report(Status.INFO, null), new Report(Status.OK, null))));
        assertEquals(Status.CRITICAL, Report.worstStatus(Arrays.asList(new Report(Status.OK, null), new Report(Status.CRITICAL, null))));
    }
    
    @Test
    public void reportMightNotHaveAValue() throws Exception {
        assertTrue(new Report(Status.INFO, "foo").hasValue());
        assertFalse(new Report(Status.INFO, null).hasValue());
        assertFalse(new Report(Status.INFO).hasValue());
    }
    
    @Test
    public void reportMightFail() throws Exception {
        assertTrue(new Report(Status.INFO, "foo").isSuccessful());
        assertTrue(new Report(Status.INFO).isSuccessful());
        
        assertFalse(new Report(Status.INFO, new Error()).isSuccessful());
        
        assertFalse(new Report(new Error()).isSuccessful());
        assertEquals(Status.CRITICAL, new Report(new Error()).getStatus());
    }

    @Test
    public void updatedReportValue() throws Exception {
        assertEquals(new Report(Status.INFO, "test (suffix)"),
                new Report(Status.INFO, "test").mapValue(v -> v + " (suffix)"));
    }

    @Test
    public void withStatusNoWorseThan() throws Exception {
        assertEquals(Status.WARNING, new Report(Status.CRITICAL, null).withStatusNoWorseThan(Status.WARNING).getStatus());
        assertEquals(Status.OK, new Report(Status.WARNING, null).withStatusNoWorseThan(Status.OK).getStatus());
        assertEquals(Status.OK, new Report(Status.CRITICAL, null).withStatusNoWorseThan(Status.OK).getStatus());
        assertEquals(Status.INFO, new Report(Status.OK, null).withStatusNoWorseThan(Status.INFO).getStatus());
    }

    @Test
    public void joinStringValues() throws Exception {
        Report r1 = new Report(Status.OK, "nothing happening");
        Report r2 = new Report(Status.CRITICAL, "this is fine");
        Report report = Report.joinStringValues(r1, r2);

        assertThat(report, equalTo(new Report(Status.CRITICAL, "nothing happening\nthis is fine")));
    }
}
