package com.timgroup.tucker.info.httpserver;

import com.codahale.metrics.MetricRegistry;
import com.timgroup.tucker.info.Component;
import com.timgroup.tucker.info.Health;
import com.timgroup.tucker.info.Report;
import com.timgroup.tucker.info.Status;
import com.timgroup.tucker.info.component.JarVersionComponent;
import com.timgroup.tucker.info.status.StatusPageGenerator;

import java.io.IOException;
import java.time.ZonedDateTime;

public class ApplicationInformationServerDemo {

    public static void main(String[] args) throws IOException {
        StatusPageGenerator statusPage = new StatusPageGenerator("tuckerDemo", new JarVersionComponent(Object.class));
        statusPage.addComponent(new TimeComponent());

        MetricRegistry metricRegistry = new MetricRegistry();
        metricRegistry.counter("my_applications_special_counter_metric").inc();

        ApplicationInformationServer server = ApplicationInformationServer.create(choosePort(args), statusPage, Health.ALWAYS_HEALTHY, metricRegistry);
        server.start();

        System.out.println(server.getBase());
    }

    private static int choosePort(String[] args) {
        return args.length > 0 ? Integer.parseInt(args[0]) : 8023;
    }

    private static final class TimeComponent extends Component {
        private TimeComponent() {
            super("time", "Time now");
        }

        @Override
        public Report getReport() {
            ZonedDateTime now = ZonedDateTime.now();
            int seconds = now.getSecond();
            Status status = seconds >= 50 ? Status.CRITICAL : seconds >= 30 ? Status.WARNING : Status.OK;
            return new Report(status, now);
        }
    }

}
