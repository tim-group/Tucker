package com.timgroup.tucker.info.async;

import static com.timgroup.tucker.info.Status.WARNING;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.timgroup.tucker.info.Component;
import com.timgroup.tucker.info.Report;

public final class AsyncComponent extends Component {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncComponent.class);
    private static final AsyncSettings DEFAULT_SETTINGS = AsyncSettings.settings();

    private volatile PerishableReport currentReport;
    private final Component wrapped;
    private final StatusUpdated statusUpdateHook;
    private final Duration repeatInterval;
    
    private AsyncComponent(Component wrapped, AsyncSettings settings) {
        super(wrapped.getId(), wrapped.getLabel(), wrapped.getRunbook());
        this.wrapped = wrapped;
        this.repeatInterval = settings.repeatInterval;
        this.statusUpdateHook = settings.statusUpdateHook;
        
        this.currentReport = new PerishableReport(
                new Report(WARNING, "Not yet run"),
                settings.clock, 
                settings.stalenessLimit);
    }
    
    public static AsyncComponent wrapping(Component component) {
        return new AsyncComponent(component, DEFAULT_SETTINGS);
    }

    public static AsyncComponent wrapping(Component component, AsyncSettings settings) {
        return new AsyncComponent(component, settings);
    }
    
    @Override
    public Report getReport() {
        return currentReport.getPotentiallyStaleReport();
    }

    public Duration getRepeatInterval() {
        return this.repeatInterval;
    }

    public long getRepeat() {
        return this.repeatInterval.toNanos();
    }
    
    public TimeUnit getRepeatTimeUnit() {
        return TimeUnit.NANOSECONDS;
    }
    
    public void update() {
        try {
            Report report = wrapped.getReport();
            update(report);
        } catch (Throwable e) {
            update(new Report(WARNING, e));
            LOGGER.error("unexpected exception in scheduled update of Tucker component {}", wrapped.getId(), e);
        }
    }
    
    private void update(Report report) {
        currentReport = currentReport.updatedWith(report);
        safelyInvokeUpdateHook(report);
    }

    private void safelyInvokeUpdateHook(Report report) {
        try {
            statusUpdateHook.accept(report);
        } catch (Exception e) {
            LOGGER.error("exception invoked update hook for component {} ", wrapped.getId(), e);
        }
    }

}