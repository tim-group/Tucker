package com.timgroup.tucker.info.async;

import com.timgroup.tucker.info.Component;
import com.timgroup.tucker.info.ComponentStateChangeCallback;
import com.timgroup.tucker.info.Report;
import com.timgroup.tucker.info.Status;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;


public class StatusChangedAdapterTest {
    private Component component = new Component("my-component-id", "My Component") {
        @Override public Report getReport() {
            return new Report(Status.INFO, "nothing to see here");
        }
    };
    private ComponentStateChangeCallback mockCallback = mock(ComponentStateChangeCallback.class);
    private StatusChangedAdapter adapter = new StatusChangedAdapter(component, mockCallback);

    @Test
    public void reportsStateChanges() {
        Report initialState = new Report(Status.OK, "seems fine");
        Report laterState = new Report(Status.WARNING, "things went wrong");

        adapter.apply(initialState);
        adapter.apply(laterState);

        verify(mockCallback).stateChanged(component, initialState, laterState);
    }

    @Test
    public void doesNotReportStateChangeOnInitialUpdate() {
        Report initialState = new Report(Status.OK);

        adapter.apply(initialState);

        verifyNoMoreInteractions(mockCallback);
    }

    @Test
    public void doesNotReportStateChangeIfOnlyValueChanges() {
        Report initialState = new Report(Status.OK, "seems fine");
        Report laterState = new Report(Status.OK, "still fine");

        adapter.apply(initialState);
        adapter.apply(laterState);

        verifyNoMoreInteractions(mockCallback);
    }

}
