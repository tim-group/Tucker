package com.timgroup.tucker.info.status;

import static com.timgroup.tucker.info.Status.WARNING;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.timgroup.tucker.info.Component;
import com.timgroup.tucker.info.Report;
import com.timgroup.tucker.info.Status;

public class AsyncComponent extends Component {

    private final Consumer statusUpdateHook;
    private final AtomicReference<Report> current = new AtomicReference<Report>();
    private final AtomicReference<Date> lastRunTimeStamp;
    private final Component wrapped;
    private final ScheduledExecutorService executor;
    private final Clock clock;
    private final long repeat;
    private final TimeUnit repeatTimeUnit;
    private final long stalenessLimit = 5;
    private final TimeUnit stalenessTimeUnit = TimeUnit.MINUTES;

    public AsyncComponent(Component wrapped, ScheduledExecutorService executor, Clock clock, long repeat,
            TimeUnit repeatTimeUnit, Consumer statusUpdateHook) {
        super(wrapped.getId(), wrapped.getLabel());
        this.wrapped = wrapped;
        this.executor = executor;
        this.clock = clock;
        this.repeat = repeat;
        this.repeatTimeUnit = repeatTimeUnit;
        this.statusUpdateHook = statusUpdateHook;
        this.lastRunTimeStamp = new AtomicReference<Date>(clock.now());
        this.current.set(new Report(Status.INFO, "Pending"));
    }

    public static AsyncComponent wrapping(Component wrapped) {
        return new AsyncComponent(wrapped, Executors.newScheduledThreadPool(1), new SystemClock(), 1, TimeUnit.MINUTES, Consumer.NOOP);
    }

    public static AsyncComponent wrapping(Component wrapped, Clock clock) {
        return new AsyncComponent(wrapped, Executors.newScheduledThreadPool(1), clock, 1, TimeUnit.MINUTES, Consumer.NOOP);
    }

    public static AsyncComponent wrapping(Component wrapped, Clock clock, long repeat, TimeUnit repeatTimeUnit, Consumer statusUpdateHook) {
        return new AsyncComponent(wrapped, Executors.newScheduledThreadPool(1), clock, repeat, repeatTimeUnit, statusUpdateHook);
    }

    public void stop() throws InterruptedException {
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.SECONDS);
    }

    public void start() {
        executor.schedule(new UpdateComponentStatusRunnable(), repeat, repeatTimeUnit);
    }
    
    class UpdateComponentStatusRunnable implements Runnable {

        @Override
        public void run() {
            Report report = safeGetWrappedReport();
            current.set(report);
            lastRunTimeStamp.set(clock.now());
            statusUpdateHook.apply(report);
            executor.schedule(this, repeat, repeatTimeUnit);
        }

        private Report safeGetWrappedReport() {
            try {
                return wrapped.getReport();
            } catch (Throwable t) {
                return new Report(t);
            }
        }
        
    }

    @Override
    public Report getReport() {
        Report report = current.get();
        Date lastRun = lastRunTimeStamp.get();

        if ((clock.now().getTime() - lastRun.getTime()) > stalenessTimeUnit.toMillis(stalenessLimit)) {
            return new Report(WARNING, 
                    String.format("Last run at %s (over %s %s ago): %s",
                            isoFormatted(lastRun), stalenessLimit, stalenessTimeUnit.name().toLowerCase(), report.getValue()));
        }
        return report;
    }

    private String isoFormatted(Date date) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        return df.format(date);
    }

    public void update() {
        this.current.set(wrapped.getReport());
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
}