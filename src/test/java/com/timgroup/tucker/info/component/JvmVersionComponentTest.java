package com.timgroup.tucker.info.component;

import com.timgroup.tucker.info.Status;
import org.hamcrest.Matchers;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeThat;

public final class JvmVersionComponentTest {
    
    @Test
    public void default_id_and_label_are_suitable() throws Exception {
        assertEquals("jvmversion", new JvmVersionComponent().getId());
        assertEquals("JVM Version", new JvmVersionComponent().getLabel());
    }

    @Test
    public void report_status_is_info() throws Exception {
        assertEquals(Status.INFO, new JvmVersionComponent().getReport().getStatus());
    }

    @Test
    public void report_java_version() throws Exception {
        String jdkVersion = System.getProperty("java.version");
        assertThat(String.valueOf(new JvmVersionComponent().getReport().getValue()), containsString(jdkVersion));
    }
}
