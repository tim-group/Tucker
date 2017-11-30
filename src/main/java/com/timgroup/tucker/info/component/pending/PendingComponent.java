package com.timgroup.tucker.info.component.pending;

import com.timgroup.tucker.info.Component;
import com.timgroup.tucker.info.ComponentStateChangeCallback;
import com.timgroup.tucker.info.Report;
import com.timgroup.tucker.info.Status;
import com.timgroup.tucker.info.log.LoggingCallback;

import static java.lang.String.format;
import static java.lang.String.valueOf;

public final class PendingComponent extends Component {

    public static final ComponentStateChangeCallback NO_OP = new ComponentStateChangeCallback() {
        @Override public void stateChanged(Component component, Report previous, Report current) { }
    };

    public static final ComponentStateChangeCallback LOGGING_CALLBACK = new LoggingCallback();

    private final Component wrappedComponent;
    private final ComponentStateChangeCallback callback;
    private volatile Report previousReportRef;

    public PendingComponent(Component wrappedComponent, ComponentStateChangeCallback callback) {
        super(wrappedComponent.getId(), wrappedComponent.getLabel() + " (pending)");
        this.wrappedComponent = wrappedComponent;
        this.callback = callback;
    }

    public PendingComponent(Component wrappedComponent) {
        this(wrappedComponent, LOGGING_CALLBACK);
    }

    @Override
    public Report getReport() {
        Report current = safelyGetReport(wrappedComponent);
        Report previous = previousReportRef;

        if (previous != null && !previous.equals(current)) {
            callback.stateChanged(wrappedComponent, previous, current);
        }
        previousReportRef = current;

        return new Report(
            Status.INFO,
            format("%s (actual status: %s)", valueOf(current.getValue()), current.getStatus()));
    }

    static Report safelyGetReport(Component wrappedComponent) {
        try {
            return wrappedComponent.getReport();
        } catch (Throwable t) {
            return new Report(t);
        }
    }
}
