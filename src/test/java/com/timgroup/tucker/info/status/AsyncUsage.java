package com.timgroup.tucker.info.status;

import static com.timgroup.tucker.info.Status.INFO;
import static java.util.Arrays.asList;

import java.util.concurrent.TimeUnit;

import com.timgroup.tucker.info.Component;
import com.timgroup.tucker.info.Health;
import com.timgroup.tucker.info.Report;
import com.timgroup.tucker.info.Status;
import com.timgroup.tucker.info.component.VersionComponent;
import com.timgroup.tucker.info.httpserver.ApplicationInformationServer;

public class AsyncUsage {

    public static void main(String[] args) throws Exception {
        final AsyncStatusPageGenerator asyncStatusPageGenerator = new AsyncStatusPageGenerator(asList(
                AsyncComponent.wrapping(new SlowComponent(), new AsyncComponent.Settings().withRepeatSchedule(10, TimeUnit.SECONDS)),
                AsyncComponent.wrapping(new QuickComponent(), new AsyncComponent.Settings().withRepeatSchedule(3, TimeUnit.SECONDS))));
        
        
        StatusPageGenerator generator = new StatusPageGenerator("my-app", versionComponent());
        
        asyncStatusPageGenerator.addAll(generator);
        asyncStatusPageGenerator.start();

        final ApplicationInformationServer server = ApplicationInformationServer.create(8001, generator, Health.ALWAYS_HEALTHY);
        server.start();
        
        Runtime runtime = Runtime.getRuntime();
        runtime.addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    asyncStatusPageGenerator.stop();
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
