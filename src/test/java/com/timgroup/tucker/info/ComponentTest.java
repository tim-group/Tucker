package com.timgroup.tucker.info;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class ComponentTest {
    @Test
    public void supplyReport() throws Exception {
        Component component = Component.of("test-id", "test label", new Report(Status.WARNING, "test-value"));
        assertThat(component.getId(), equalTo("test-id"));
        assertThat(component.getLabel(), equalTo("test label"));
        assertThat(component.getReport(), equalTo(new Report(Status.WARNING, "test-value")));
    }

    @Test
    public void supplyInfo() throws Exception {
        Component component = Component.info("test-id", "test label", "test-value");
        assertThat(component.getId(), equalTo("test-id"));
        assertThat(component.getLabel(), equalTo("test label"));
        assertThat(component.getReport(), equalTo(new Report(Status.INFO, "test-value")));
    }

    @Test
    public void mapReport() throws Exception {
        Component component = Component.info("test-id", "test label", "test-value")
                .mapReport(report -> report.mapValue(str -> str + " (suffix)"));
        assertThat(component.getId(), equalTo("test-id"));
        assertThat(component.getLabel(), equalTo("test label"));
        assertThat(component.getReport(), equalTo(new Report(Status.INFO, "test-value (suffix)")));
    }

    @Test
    public void mapValue() throws Exception {
        Component component = Component.info("test-id", "test label", "test-value")
                .mapValue(str -> str + " (suffix)");
        assertThat(component.getId(), equalTo("test-id"));
        assertThat(component.getLabel(), equalTo("test label"));
        assertThat(component.getReport(), equalTo(new Report(Status.INFO, "test-value (suffix)")));
    }

    @Test
    public void joinStringValues() throws Exception {
        Component c1 = Component.of("test-1", "Test 1", new Report(Status.OK, "nothing happening"));
        Component c2 = Component.of("test-2", "Test 2", new Report(Status.CRITICAL, "this is fine"));
        Component component = Component.joinStringValues("combined", "Combined", c1, c2);

        assertThat(component.getId(), equalTo("combined"));
        assertThat(component.getLabel(), equalTo("Combined"));
        assertThat(component.getReport(), equalTo(new Report(Status.CRITICAL, "nothing happening\nthis is fine")));
    }
}
