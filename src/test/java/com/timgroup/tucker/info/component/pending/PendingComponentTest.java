package com.timgroup.tucker.info.component.pending;

import static java.lang.String.valueOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;

import com.timgroup.tucker.info.Component;
import com.timgroup.tucker.info.Report;
import com.timgroup.tucker.info.Status;
import com.timgroup.tucker.info.component.pending.ComponentStateChangeCallback;
import com.timgroup.tucker.info.component.pending.PendingComponent;

public class PendingComponentTest {

    private final Component criticalComponent = new Component("always-critical", "Critical Label") {
        @Override public Report getReport() {
            return new Report(Status.CRITICAL, "I am critical");
        }

    };

    @Test public void
    returnsInfoStatusRegardlessOfWrappedComponentStatus() {
        PendingComponent pending = new PendingComponent(criticalComponent, PendingComponent.NO_OP);

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
        Component changingStatusComponent = new Component("changing", "Changing") {
            private boolean first = true;
            @Override public Report getReport() {
                Report report = first ? new Report(Status.OK, "I'm fine") : new Report(Status.CRITICAL, "Now I'm sad");
                first = false;
                return report;
            }
        };

        ComponentStateChangeCallback callback = mock(ComponentStateChangeCallback.class);

        PendingComponent pending = new PendingComponent(changingStatusComponent, callback);

        pending.getReport();
        pending.getReport();

        verify(callback).stateChanged(changingStatusComponent, new Report(Status.OK, "I'm fine"), new Report(Status.CRITICAL, "Now I'm sad"));
    }

}
