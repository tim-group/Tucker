package com.timgroup.tucker.info.component.pending;

import com.timgroup.tucker.info.Component;
import com.timgroup.tucker.info.ComponentStateChangeCallback;
import com.timgroup.tucker.info.Report;
import com.timgroup.tucker.info.Status;
import org.junit.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.String.valueOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class PendingUntilComponentTest {

    private final Component criticalComponent = Component.of("always-critical", "Critical Label", new Report(Status.CRITICAL, "I am critical"));

    private final Instant pendingUntil = Instant.parse("2017-11-30T12:47:33.651Z");
    private final Clock clockJustBeforePendingUntil = Clock.fixed(pendingUntil.minusSeconds(1), ZoneId.systemDefault());
    private final Clock clockJustAfterPendingUntil =  Clock.fixed(pendingUntil.plusSeconds(1), ZoneId.systemDefault());

    @Test public void
    returnsInfoStatusRegardlessOfWrappedComponentStatusWhenBeforePendingUntil() {
        PendingUntilComponent pending = new PendingUntilComponent(criticalComponent,
                pendingUntil,
                clockJustBeforePendingUntil,
                PendingComponent.NO_OP);

        assertThat(pending.getReport().getStatus(), is(Status.INFO));
    }

    @Test public void
    returnsInfoStatusIfWrappedComponentThrowsUncaughtException() {
        Component exceptionThrowingComponent = Component.supplyReport("test", "test", () -> { throw new IllegalStateException("I will always throw an exception"); });
        PendingUntilComponent pending = new PendingUntilComponent(exceptionThrowingComponent, pendingUntil, clockJustBeforePendingUntil, PendingComponent.NO_OP);

        assertThat(pending.getReport().getStatus(), is(Status.INFO));
    }

    @Test public void
    returnsValueOfWrappedComponent() {
        PendingUntilComponent pending = new PendingUntilComponent(criticalComponent, pendingUntil, clockJustBeforePendingUntil, PendingComponent.NO_OP);
        String value = valueOf(pending.getReport().getValue());

        assertThat(value, is("I am critical (actual status: CRITICAL)"));
    }

    @Test public void
    afterPendingUntilReturnsActualResult() {
        PendingUntilComponent pending = new PendingUntilComponent(criticalComponent, pendingUntil, clockJustAfterPendingUntil, PendingComponent.NO_OP);

        assertThat(pending.getReport(), is(criticalComponent.getReport()));
    }

    @Test public void
    notifiesCallBackOfComponentStateChange() {
        AtomicReference<Report> theReport = new AtomicReference<>(new Report(Status.OK, "I'm fine"));
        Component changingStatusComponent = Component.supplyReport("test", "test", theReport::get);

        ComponentStateChangeCallback callback = mock(ComponentStateChangeCallback.class);

        PendingUntilComponent pending = new PendingUntilComponent(changingStatusComponent, pendingUntil, clockJustBeforePendingUntil, callback);

        pending.getReport();

        theReport.set(new Report(Status.CRITICAL, "Now I'm sad"));

        pending.getReport();

        verify(callback).stateChanged(changingStatusComponent, new Report(Status.OK, "I'm fine"), new Report(Status.CRITICAL, "Now I'm sad"));
    }

}
