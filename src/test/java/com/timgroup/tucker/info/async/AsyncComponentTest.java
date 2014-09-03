package com.timgroup.tucker.info.async;

import static com.timgroup.tucker.info.Status.OK;
import static com.timgroup.tucker.info.Status.WARNING;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

import com.timgroup.tucker.info.Component;
import com.timgroup.tucker.info.Report;
import com.timgroup.tucker.info.async.AsyncComponent;
import org.mockito.Matchers;
import org.mockito.Mockito;

public class AsyncComponentTest {

    private final Component healthyWellBehavedComponent = new Component("my-test-component-id", "My Test Component Label") {
        @Override public Report getReport() {
            return new Report(OK, "It's all good.");
        }
    };
    
    @Test
    public void returnsIdAndLabelOfWrappedComponent() {
        AsyncComponent asyncComponent = AsyncComponent.wrapping(healthyWellBehavedComponent);
        assertEquals("my-test-component-id", asyncComponent.getId());
        assertEquals("My Test Component Label", asyncComponent.getLabel());
    }
    
    @Test
    public void returnsPendingReportForWrappedComponentThatHasNotReturnedYet() {
        AsyncComponent asyncComponent = AsyncComponent.wrapping(healthyWellBehavedComponent);

        Report report = asyncComponent.getReport();

        assertEquals(report.getStatus(), WARNING);
        assertEquals(report.getValue(), "Pending");
    }
    
    @Test
    public void returnsReportCreatedByWrappedComponent() {
        AsyncComponent asyncComponent = AsyncComponent.wrapping(healthyWellBehavedComponent);
        
        asyncComponent.update();
        
        assertEquals(
            new Report(OK, "It's all good."),
            asyncComponent.getReport());
    }

    @Test
    public void returnsWarningWhenComponentFails() {
        Component failingComponent = Mockito.mock(Component.class);
        Error error = new Error();
        Mockito.when(failingComponent.getReport()).thenThrow(error);
        AsyncComponent asyncComponent = AsyncComponent.wrapping(failingComponent);

        asyncComponent.update();

        Report report = asyncComponent.getReport();
        assertEquals(report.getStatus(), WARNING);
        assertEquals(report.getValue(), error);
    }
}