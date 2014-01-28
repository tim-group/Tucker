package com.timgroup.tucker.demo;

import com.timgroup.tucker.info.Component;
import com.timgroup.tucker.info.Health;
import com.timgroup.tucker.info.Report;
import com.timgroup.tucker.info.Status;
import com.timgroup.tucker.info.Stoppable;
import com.timgroup.tucker.info.component.JarVersionComponent;
import com.timgroup.tucker.info.servlet.ApplicationInformationServlet;
import com.timgroup.tucker.info.status.StatusPageGenerator;

@SuppressWarnings("serial")
public class DemoStatusPageServlet extends ApplicationInformationServlet {

    public DemoStatusPageServlet() {
        super(statusPage(), Stoppable.ALWAYS_STOPPABLE, Health.ALWAYS_HEALTHY);
    }

    private static StatusPageGenerator statusPage() {
        StatusPageGenerator statusPage = new StatusPageGenerator("demoApp", new JarVersionComponent(StatusPageGenerator.class));
        statusPage.addComponent(new TimeUsedComponent());
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

}
