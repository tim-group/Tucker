package com.timgroup.tucker.test.jetty;

import com.timgroup.tucker.info.Health;
import com.timgroup.tucker.info.Stoppable;
import com.timgroup.tucker.info.servlet.ApplicationInformationServlet;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.eclipse.jetty.server.CustomRequestLog;
import org.eclipse.jetty.server.NetworkConnector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Slf4jRequestLogWriter;
import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.slf4j.LoggerFactory.getLogger;

public class ServletInDedicatedContextTest {
    private static final Logger LOG = getLogger(ServletInDedicatedContextTest.class);

    public static ApplicationInformationServlet demoServlet() {
        return new ApplicationInformationServlet("ServletInRootContextTest-demo", Stoppable.ALWAYS_STOPPABLE, Health.ALWAYS_HEALTHY);
    }

    @Rule
    public ServerRule server = new ServerRule();

    @Test
    public void provides_status_resources() throws Exception {
        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(new HttpGet(String.format("http://localhost:%d/info/status", server.getPort())))) {
            assertThat(response.getStatusLine().getStatusCode(), equalTo(200));
            assertThat(EntityUtils.toString(response.getEntity()), containsString("ServletInRootContextTest-demo"));
        }
    }

    public static final class ServerRule extends ExternalResource {
        private final Server server;

        public ServerRule() {
            server = new Server(0);

            ServletContextHandler servletContextHandler = new ServletContextHandler();
            servletContextHandler.setContextPath("/info");
            servletContextHandler.setGzipHandler(new GzipHandler());
            servletContextHandler.addServlet(DefaultServlet.class, "/");
            servletContextHandler.addServlet(new ServletHolder("tucker", demoServlet()), "/*");

            StatisticsHandler statisticsHandler = new StatisticsHandler();
            statisticsHandler.setHandler(servletContextHandler);

            server.setRequestLog(new CustomRequestLog(new Slf4jRequestLogWriter(), CustomRequestLog.EXTENDED_NCSA_FORMAT));
            server.setHandler(statisticsHandler);
        }

        @Override
        protected void before() throws Throwable {
            server.start();
        }

        @Override
        protected void after() {
            try {
                server.stop();
            } catch (Exception e) {
                LOG.warn("Ignoring error stopping Jetty", e);
            }
        }

        public int getPort() {
            return ((NetworkConnector) server.getConnectors()[0]).getLocalPort();
        }
    }
}
