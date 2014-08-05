package com.timgroup.tucker.info.status;

import static com.timgroup.tucker.info.Status.WARNING;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.timgroup.tucker.info.Component;
import com.timgroup.tucker.info.Report;

public class AsyncComponent extends Component {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncComponent.class);

    private final AtomicReference<PerishableReport> currentReport;
    
    private final Component wrapped;
    private final Consumer statusUpdateHook;
    final long repeat;
    final TimeUnit repeatTimeUnit;
    
    private AsyncComponent(Component wrapped) {
        this(wrapped, new Settings());
    }
    
    private AsyncComponent(Component wrapped, Settings settings) {
        super(wrapped.getId(), wrapped.getLabel());
        this.wrapped = wrapped;
        this.repeat = settings.repeat;
        this.repeatTimeUnit = settings.repeatTimeUnit;
        this.statusUpdateHook = settings.statusUpdateHook;
        
        PerishableReport initialReport = new PerishableReport(
                new Report(WARNING, "Pending"), 
                settings.clock, 
                settings.stalenessLimit, 
                settings.stalenessTimeUnit);
        this.currentReport = new AtomicReference<PerishableReport>(initialReport);
    }
    
    public static AsyncComponent wrapping(Component component) {
        return new AsyncComponent(component, new Settings());
    }

    public static AsyncComponent wrapping(Component component, Settings settings) {
        return new AsyncComponent(component, settings);
    }

    public static final class Settings {
        private Clock clock = new SystemClock();
        private long repeat = 30;
        private TimeUnit repeatTimeUnit = TimeUnit.SECONDS;
        private Consumer statusUpdateHook = Consumer.NOOP;
        private long stalenessLimit = 5;
        private TimeUnit stalenessTimeUnit = TimeUnit.MINUTES;
        
        public Settings withClock(Clock clock) { this.clock = clock; return this; }
        public Settings withRepeatSchedule(long time, TimeUnit units) { this.repeat = time; this.repeatTimeUnit = units; return this; }
        public Settings withUpdateHook(Consumer statusUpdated) { this.statusUpdateHook = statusUpdated; return this; }
        public Settings withStalenessLimit(long time, TimeUnit units) { this.stalenessLimit = time; this.stalenessTimeUnit = units; return this; }
    }
    
    final class UpdateComponentStatusRunnable implements Runnable {

        @Override
        public void run() {
            Report report = safeGetWrappedReport();
            currentReport.set(currentReport.get().updatedWith(report));
            safelyInvokeUpdateHook(report);
        }

        private Report safeGetWrappedReport() {
            try {
                return wrapped.getReport();
            } catch (Throwable t) {
                return new Report(t);
            }
        }
        
        private void safelyInvokeUpdateHook(Report report) {
            try {
                statusUpdateHook.apply(report);
            } catch (Throwable t) {
                LOGGER.error("exception invoked update hook for component {} ", wrapped.getId(), t);
            }
        }
    }
    

    @Override
    public Report getReport() {
        return currentReport.get().getPotentiallyStaleReport();
    }

    public interface Clock {
        Date now();
    }

    public static class SystemClock implements Clock {
        @Override
        public Date now() {
            return new Date();
        }
    }
    
    public static interface Consumer {
        void apply(Report report);
        
        public static final Consumer NOOP = new Consumer() {
            @Override public void apply(Report report) { }
        };
    }

    public Runnable updateStatusRunnable() {
        return new UpdateComponentStatusRunnable();
    }

}