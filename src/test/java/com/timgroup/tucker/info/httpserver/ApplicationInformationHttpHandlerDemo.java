package com.timgroup.tucker.info.httpserver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;

import com.sun.net.httpserver.HttpServer;
import com.timgroup.tucker.info.ApplicationInformationHandler;
import com.timgroup.tucker.info.Component;
import com.timgroup.tucker.info.Health;
import com.timgroup.tucker.info.Report;
import com.timgroup.tucker.info.Status;
import com.timgroup.tucker.info.Stoppable;
import com.timgroup.tucker.info.component.JarVersionComponent;
import com.timgroup.tucker.info.status.StatusPageGenerator;

public class ApplicationInformationHttpHandlerDemo {

    public static void main(String[] args) throws IOException, URISyntaxException {
        URI base = new URI("http", null, InetAddress.getLocalHost().getHostName(), choosePort(args), "/info", null, null);

        ApplicationInformationHandler handler = prepareHandler();

        HttpServer server = HttpServer.create(new InetSocketAddress(base.getPort()), 0);
        server.createContext(base.getPath(), new ApplicationInformationHttpHandler(handler, base));
        server.setExecutor(null);
        server.start();

        System.out.println(base);
    }

    private static ApplicationInformationHandler prepareHandler() {
        StatusPageGenerator statusPage = new StatusPageGenerator("tuckerDemo", new JarVersionComponent(Object.class));
        Stoppable stoppable = Stoppable.ALWAYS_STOPPABLE;
        Health health = Health.ALWAYS_HEALTHY;
        statusPage.addComponent(new TimeComponent());
        return new ApplicationInformationHandler(statusPage, stoppable, health);
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
            Calendar now = Calendar.getInstance();
            int seconds = now.get(Calendar.SECOND);
            Status status = seconds >= 50 ? Status.CRITICAL : seconds >= 30 ? Status.WARNING : Status.OK;
            return new Report(status, now.getTime());
        }
    }

}
