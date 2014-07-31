package com.timgroup.tucker.info.status;

import static java.util.concurrent.TimeUnit.MINUTES;

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

    public AsyncComponent(Component wrapped, ScheduledExecutorService executor, Clock clock, long repeat,
            TimeUnit repeatTimeUnit, Consumer statusUpdateHook) {
        super(wrapped.getId(), wrapped.getLabel());
        this.wrapped = wrapped;
        this.executor = executor;
        this.clock = clock;
        this.repeat = repeat;
        this.repeatTimeUnit = repeatTimeUnit;
        this.statusUpdateHook = statusUpdateHook;
        this.lastRunTimeStamp = new AtomicReference<Date>(getNow("initialisation"));
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
            Report report = wrapped.getReport();
            current.set(report);
            lastRunTimeStamp.set(getNow("update"));
            statusUpdateHook.apply(report);
            executor.schedule(this, repeat, repeatTimeUnit);
        }
        
    }

    @Override
    public Report getReport() {
        Report report = current.get();
        Date lastRun = lastRunTimeStamp.get();

        if ((getNow("getReport").getTime() - lastRun.getTime()) > MINUTES.toMillis(5)) {
            return new Report(Status.WARNING, String.format("Last run at %s (over 5 minutes ago): %s",
                    isoFormatted(lastRun), report.getValue()));
        }
        return report;
    }

    private Date getNow(String purpose) {
        Date date = clock.now();
        System.out.println(String.format("Retrieved now for %s: %s", purpose, isoFormatted(date)));
        return date;
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