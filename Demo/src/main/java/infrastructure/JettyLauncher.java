package infrastructure;

import static java.lang.Integer.parseInt;

import java.io.IOException;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.timgroup.status.demo.DemoStatusPageServlet;

public class JettyLauncher {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(JettyLauncher.class);

    private final Server server;
    private final ServletContextHandler context = new ServletContextHandler();

    public JettyLauncher(int port) {
        this.server = new Server(port);
    }

    private void setupWebApp() throws IOException {
        context.setContextPath("/");
        context.setMaxFormContentSize(0);
        context.getServletHandler().setStartWithUnavailable(false);
        context.addServlet(new ServletHolder(new DemoStatusPageServlet()),
                "/status/*");
        server.setHandler(context);
    }

    public void sink() throws Exception {
        server.stop();
    }

    public void launch() throws Exception {
        LOGGER.info("Setting up web application");
        setupWebApp();

        try {
            LOGGER.info("Starting server");
            server.start();
            LOGGER.info("Started server");
        } catch (Exception e) {
            LOGGER.error("Couldn't start Jetty", e);
            stopQuietly();
            throw e;
        }
    }

    private void stopQuietly() {
        try {
            server.stop();
        } catch (Exception e) {
            LOGGER.error("Couldn't stop Jetty either", e);
        }
    }

    public static void main(String[] args) throws Exception {
        new JettyLauncher(parseInt(args[0])).launch();
    }

}