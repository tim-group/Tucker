package com.timgroup.tucker.info.async;

import static com.timgroup.tucker.info.Status.WARNING;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.timgroup.tucker.info.Component;
import com.timgroup.tucker.info.Report;

public class AsyncComponent extends Component {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncComponent.class);
    private static final AsyncSettings DEFAULT_SETTINGS = AsyncSettings.settings();

    private final AtomicReference<PerishableReport> currentReport;
    private final Component wrapped;
    private final StatusUpdated statusUpdateHook;
    private final long repeat;
    private final TimeUnit repeatTimeUnit;
    
    
    private AsyncComponent(Component wrapped, AsyncSettings settings) {
        super(wrapped.getId(), wrapped.getLabel());
        this.wrapped = wrapped;
        this.repeat = settings.repeat;
        this.repeatTimeUnit = settings.repeatTimeUnit;
        this.statusUpdateHook = settings.statusUpdateHook;
        
        PerishableReport initialReport = new PerishableReport(
                new Report(WARNING, "Not yet run"),
                settings.clock, 
                settings.stalenessLimit, 
                settings.stalenessTimeUnit);
        this.currentReport = new AtomicReference<PerishableReport>(initialReport);
    }
    
    public static AsyncComponent wrapping(Component component) {
        return new AsyncComponent(component, DEFAULT_SETTINGS);
    }

    public static AsyncComponent wrapping(Component component, AsyncSettings settings) {
        return new AsyncComponent(component, settings);
    }
    
    @Override
    public Report getReport() {
        return currentReport.get().getPotentiallyStaleReport();
    }

    public long getRepeat() {
        return this.repeat;
    }
    
    public TimeUnit getRepeatTimeUnit() {
        return this.repeatTimeUnit;
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
        currentReport.set(currentReport.get().updatedWith(report));
        safelyInvokeUpdateHook(report);
    }

    private void safelyInvokeUpdateHook(Report report) {
        try {
            statusUpdateHook.apply(report);
        } catch (Exception e) {
            LOGGER.error("exception invoked update hook for component {} ", wrapped.getId(), e);
        }
    }

}