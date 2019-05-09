package com.timgroup.tucker.info.component.pending;

import com.timgroup.tucker.info.Component;
import com.timgroup.tucker.info.ComponentStateChangeCallback;
import com.timgroup.tucker.info.Report;
import com.timgroup.tucker.info.Status;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

import static java.lang.String.valueOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class PendingComponentTest {

    private final Component criticalComponent = Component.of("always-critical", "Critical Label", new Report(Status.CRITICAL, "I am critical"));

    @Test public void
    returnsInfoStatusRegardlessOfWrappedComponentStatus() {
        PendingComponent pending = new PendingComponent(criticalComponent, PendingComponent.NO_OP);

        assertThat(pending.getReport().getStatus(), is(Status.INFO));
    }

    @Test public void
    returnsInfoStatusIfWrappedComponentThrowsUncaughtException() {
        Component exceptionThrowingComponent = Component.supplyReport("test", "test", () -> { throw new IllegalStateException("I will always throw an exception"); });
        PendingComponent pending = new PendingComponent(exceptionThrowingComponent, PendingComponent.NO_OP);

        assertThat(pending.getReport().getStatus(), is(Status.INFO));
    }

    @Test public void
    returnsValueOfWrappedComponent() {
        PendingComponent pending = new PendingComponent(criticalComponent, PendingComponent.NO_OP);
        String value = valueOf(pending.getReport().getValue());

        assertThat(value, is("I am critical (actual status: CRITICAL)"));
    }

    @Test public void
    notifiesCallBackOfComponentStateChange() {
        AtomicReference<Report> theReport = new AtomicReference<>(new Report(Status.OK, "I'm fine"));
        Component changingStatusComponent = Component.supplyReport("test", "test", theReport::get);

        ComponentStateChangeCallback callback = mock(ComponentStateChangeCallback.class);

        PendingComponent pending = new PendingComponent(changingStatusComponent, callback);

        pending.getReport();

        theReport.set(new Report(Status.CRITICAL, "Now I'm sad"));

        pending.getReport();

        verify(callback).stateChanged(changingStatusComponent, new Report(Status.OK, "I'm fine"), new Report(Status.CRITICAL, "Now I'm sad"));
    }

}
