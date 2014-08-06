package com.timgroup.tucker.info.async;

import static com.timgroup.tucker.info.Status.INFO;
import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.SECONDS;

import com.timgroup.tucker.info.Component;
import com.timgroup.tucker.info.Health;
import com.timgroup.tucker.info.Report;
import com.timgroup.tucker.info.Status;
import com.timgroup.tucker.info.async.AsyncComponentScheduler;
import com.timgroup.tucker.info.component.VersionComponent;
import com.timgroup.tucker.info.httpserver.ApplicationInformationServer;
import com.timgroup.tucker.info.status.StatusPageGenerator;

public class AsyncUsage {

    public static void main(String[] args) throws Exception {
        final AsyncComponentScheduler scheduler = AsyncComponentScheduler.createFromSynchronous(
                AsyncSettings.settings().withRepeatSchedule(10, SECONDS),
                asList(new SlowComponent(), new QuickComponent()));
        
        
        StatusPageGenerator generator = new StatusPageGenerator("my-app", versionComponent());
        
        scheduler.addComponentsTo(generator);
        scheduler.start();

        final ApplicationInformationServer server = ApplicationInformationServer.create(8001, generator, Health.ALWAYS_HEALTHY);
        server.start();
        
        Runtime runtime = Runtime.getRuntime();
        runtime.addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    scheduler.stop();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        runtime.addShutdownHook(new Thread() {
            @Override public void run() { server.stop(); }
        });
    }

    private static final VersionComponent versionComponent() {
        return new VersionComponent() {
            @Override
            public Report getReport() {
                return new Report(INFO, "0.0.1");
            }
        };
    }

    public static final class SlowComponent extends Component {

        public SlowComponent() {
            super("slowComponent", "Slow Component");
        }

        @Override
        public Report getReport() {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) { }
            return new Report(Status.OK, "All Good");
        }
    }

    public static final class QuickComponent extends Component {
        
        public QuickComponent() {
            super("quickComponent", "Quick Component");
        }
        
        @Override
        public Report getReport() {
            return new Report(Status.OK, "All Good");
        }
    }
}
