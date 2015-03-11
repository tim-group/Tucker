package com.timgroup.tucker.info.component.pending;

import static java.lang.String.format;
import static java.lang.String.valueOf;

import java.util.concurrent.atomic.AtomicReference;


import com.timgroup.tucker.info.Component;
import com.timgroup.tucker.info.ComponentStateChangeCallback;
import com.timgroup.tucker.info.Report;
import com.timgroup.tucker.info.Status;
import com.timgroup.tucker.info.log.LoggingCallback;

public final class PendingComponent extends Component {

    public static final ComponentStateChangeCallback NO_OP = new ComponentStateChangeCallback() {
        @Override public void stateChanged(Component component, Report previous, Report current) { }
    };

    public static final ComponentStateChangeCallback LOGGING_CALLBACK = new LoggingCallback();

    private final Component wrappedComponent;
    private final ComponentStateChangeCallback callback;
    private final AtomicReference<Report> previousReportRef;

    public PendingComponent(Component wrappedComponent, ComponentStateChangeCallback callback) {
        super(wrappedComponent.getId(), wrappedComponent.getLabel() + " (pending)");
        this.wrappedComponent = wrappedComponent;
        this.callback = callback;
        this.previousReportRef = new AtomicReference<Report>();
    }

    public PendingComponent(Component wrappedComponent) {
        this(wrappedComponent, LOGGING_CALLBACK);
    }

    @Override
    public Report getReport() {
        Report current = wrappedComponent.getReport();
        Report previous = previousReportRef.get();

        if (previous != null && !previous.equals(current)) {
            callback.stateChanged(wrappedComponent, previous, current);
        }
        previousReportRef.set(current);

        return new Report(
            Status.INFO,
            format("%s (actual status: %s)", valueOf(current.getValue()), current.getStatus()));
    }

}
