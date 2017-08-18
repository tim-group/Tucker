package com.timgroup.tucker.info.async;

import java.net.URI;
import java.time.Duration;

import com.timgroup.tucker.info.Component;
import com.timgroup.tucker.info.Report;
import com.timgroup.tucker.info.Status;
import org.junit.Test;

import static com.timgroup.tucker.info.Status.INFO;
import static com.timgroup.tucker.info.Status.OK;
import static com.timgroup.tucker.info.Status.WARNING;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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
        assertEquals(report.getValue(), "Not yet run");
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
        Error error = new Error();
        Component failingComponent = new Component("test", "test") {
            @Override
            public Report getReport() {
                throw error;
            }
        };
        AsyncComponent asyncComponent = AsyncComponent.wrapping(failingComponent);

        asyncComponent.update();

        Report report = asyncComponent.getReport();
        assertEquals(report.getStatus(), WARNING);
        assertEquals(report.getValue(), error);
    }

    @Test
    public void mapping_report_of_an_async_component_returns_cloned_async_component() {
        StatusUpdated updateHook = mock(StatusUpdated.class);
        AsyncComponentListener asyncComponentListener = mock(AsyncComponentListener.class);

        AsyncSettings settings = AsyncSettings.settings().withRepeatSchedule(Duration.ofSeconds(1)).withUpdateHook(updateHook);
        AsyncComponent asyncComponent = AsyncComponent.wrapping(Component.supplyInfo("test", "test", () -> "test"), settings).withListener(asyncComponentListener);

        Component wrapped = asyncComponent.mapValue(v -> v + "test").withRunbook(URI.create("http://www.example.com")).withStatusNoWorseThan(Status.WARNING);

        assertThat(wrapped, instanceOf(AsyncComponent.class));
        assertThat(((AsyncComponent) wrapped).getRepeatInterval(), equalTo(Duration.ofSeconds(1)));
        ((AsyncComponent) wrapped).update();
        assertThat(wrapped.getReport(), equalTo(new Report(INFO, "testtest")));
        verify(updateHook).accept(eq(new Report(INFO, "testtest")));
        verify(asyncComponentListener).accept(any(AsyncComponent.class), eq(new Report(INFO, "testtest")));
    }
}
