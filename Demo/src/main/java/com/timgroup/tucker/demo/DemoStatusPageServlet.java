package com.timgroup.tucker.demo;

import com.codahale.metrics.MetricRegistry;
import com.timgroup.tucker.info.Component;
import com.timgroup.tucker.info.Health;
import com.timgroup.tucker.info.Report;
import com.timgroup.tucker.info.Runbook;
import com.timgroup.tucker.info.Status;
import com.timgroup.tucker.info.Stoppable;
import com.timgroup.tucker.info.component.JarVersionComponent;
import com.timgroup.tucker.info.servlet.ApplicationInformationServlet;
import com.timgroup.tucker.info.status.StatusPageGenerator;

@SuppressWarnings("serial")
public class DemoStatusPageServlet extends ApplicationInformationServlet {

    public DemoStatusPageServlet() {
        super(statusPage(), Stoppable.ALWAYS_STOPPABLE, Health.ALWAYS_HEALTHY, new MetricRegistry());
    }

    private static StatusPageGenerator statusPage() {
        StatusPageGenerator statusPage = new StatusPageGenerator("demoApp", new JarVersionComponent(StatusPageGenerator.class));
        statusPage.addComponent(new TimeUsedComponent());
        statusPage.addComponent(new HaveNotHeardEnoughSwearingRecentlyComponent());
        return statusPage;
    }

    private static final class TimeUsedComponent extends Component {
        public TimeUsedComponent() {
            super("timeUsed", "Time used this minute (sec)");
        }

        @Override
        public Report getReport() {
            long seconds = (System.currentTimeMillis() / 1000) % 60;
            Status status = seconds >= 50 ? Status.CRITICAL : seconds >= 30 ? Status.WARNING : Status.OK;
            return new Report(status, seconds);
        }
    }

    private static final class HaveNotHeardEnoughSwearingRecentlyComponent extends Component {
        public HaveNotHeardEnoughSwearingRecentlyComponent() {
            super("notHeardEnoughSwearing", "Have you not heard enough swearing recently?");
        }

        @Override
        public Report getReport() {
            return new Report(Status.WARNING, "Severe lack of in-ear profanity",
                    new Runbook("https://www.youtube.com/watch?v=P1rRszEYKdM"));
        }
    }

}
