package com.timgroup.tucker.info.status;

import static com.timgroup.tucker.info.Status.WARNING;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.timgroup.tucker.info.Component;
import com.timgroup.tucker.info.Report;

public class AsyncComponent extends Component {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncComponent.class);
    private static final Settings DEFAULT_SETTINGS = settings();

    private final AtomicReference<PerishableReport> currentReport;
    private final Component wrapped;
    private final Consumer statusUpdateHook;
    final long repeat;
    final TimeUnit repeatTimeUnit;
    
    
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
        return new AsyncComponent(component, DEFAULT_SETTINGS);
    }

    public static AsyncComponent wrapping(Component component, Settings settings) {
        return new AsyncComponent(component, settings);
    }
    
    public static Settings settings() {
        return new Settings(new SystemClock(), 30, SECONDS, Consumer.NOOP, 5, MINUTES);
    }

    public static final class Settings {
        private final Clock clock;
        private final long repeat;
        private final TimeUnit repeatTimeUnit;
        private final Consumer statusUpdateHook;
        private final long stalenessLimit;
        private final TimeUnit stalenessTimeUnit;
        
        public Settings(Clock clock, long repeat, TimeUnit repeatTimeUnit, Consumer statusUpdateHook,
                long stalenessLimit, TimeUnit stalenessTimeUnit) {
            this.clock = clock;
            this.repeat = repeat;
            this.repeatTimeUnit = repeatTimeUnit;
            this.statusUpdateHook = statusUpdateHook;
            this.stalenessLimit = stalenessLimit;
            this.stalenessTimeUnit = stalenessTimeUnit;
        }
        public Settings withClock(Clock clock) { 
            return new Settings(clock, repeat, repeatTimeUnit, statusUpdateHook, stalenessLimit, stalenessTimeUnit); 
        }
        public Settings withRepeatSchedule(long time, TimeUnit units) { 
            return new Settings(clock, time, units, statusUpdateHook, stalenessLimit, stalenessTimeUnit); 
        }
        public Settings withUpdateHook(Consumer statusUpdated) { 
            return new Settings(clock, repeat, repeatTimeUnit, statusUpdated, stalenessLimit, stalenessTimeUnit); 
        }
        public Settings withStalenessLimit(long time, TimeUnit units) { 
            return new Settings(clock, repeat, repeatTimeUnit, statusUpdateHook, time, units); 
        }
    }
    
    final class UpdateComponentStatusRunnable implements Runnable {

        @Override
        public void run() {
            try {
                Report report = wrapped.getReport();
                update(report);
            } catch (Exception e) {
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