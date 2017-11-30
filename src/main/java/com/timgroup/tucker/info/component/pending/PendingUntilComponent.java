package com.timgroup.tucker.info.component.pending;

import com.timgroup.tucker.info.Component;
import com.timgroup.tucker.info.ComponentStateChangeCallback;
import com.timgroup.tucker.info.Report;
import com.timgroup.tucker.info.Status;

import java.time.Clock;
import java.time.Instant;

import static com.timgroup.tucker.info.component.pending.PendingComponent.LOGGING_CALLBACK;
import static com.timgroup.tucker.info.component.pending.PendingComponent.safelyGetReport;
import static java.lang.String.format;
import static java.lang.String.valueOf;

public final class PendingUntilComponent extends Component {
    private final Component wrappedComponent;
    private final Instant pendingUntil;
    private final Clock clock;
    private final ComponentStateChangeCallback callback;
    private volatile Report previousReportRef;

    public PendingUntilComponent(Component wrappedComponent, Instant pendingUntil, Clock clock, ComponentStateChangeCallback callback) {
        super(wrappedComponent.getId(), wrappedComponent.getLabel() + " (pending until " + pendingUntil + ")");
        this.wrappedComponent = wrappedComponent;
        this.pendingUntil = pendingUntil;
        this.clock = clock;
        this.callback = callback;
    }

    public PendingUntilComponent(Component wrappedComponent, Instant pendingUntil) {
        this(wrappedComponent, pendingUntil, Clock.systemUTC(), LOGGING_CALLBACK);
    }

    @Override
    public Report getReport() {
        Report current = safelyGetReport(wrappedComponent);
        Report previous = previousReportRef;

        if (previous != null && !previous.equals(current)) {
            callback.stateChanged(wrappedComponent, previous, current);
        }
        previousReportRef = current;

        if (clock.instant().isBefore(pendingUntil)) {
            return new Report(
                    Status.INFO,
                    format("%s (actual status: %s)", valueOf(current.getValue()), current.getStatus()));
        } else {
            return current;
        }
    }
}
