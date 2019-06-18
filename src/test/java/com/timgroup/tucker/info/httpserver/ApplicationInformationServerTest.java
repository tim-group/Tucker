package com.timgroup.tucker.info.httpserver;

import com.codahale.metrics.MetricRegistry;
import com.timgroup.tucker.info.component.JarVersionComponent;
import com.timgroup.tucker.info.status.StatusPageGenerator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import static com.timgroup.tucker.info.Health.ALWAYS_HEALTHY;
import static com.timgroup.tucker.info.Stoppable.ALWAYS_STOPPABLE;
import static com.timgroup.tucker.info.httpserver.ApplicationInformationServer.create;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;

public class ApplicationInformationServerTest {
    StatusPageGenerator statusPage;
    ApplicationInformationServer server;


    @Before public void
    startServer() throws IOException {
        statusPage = new StatusPageGenerator("test-tucker", new JarVersionComponent(Object.class));
        server = create(0, statusPage, ALWAYS_STOPPABLE, ALWAYS_HEALTHY, new MetricRegistry());
        server.start();
    }

    @After public void
    stopServer() {
        server.stop();
    }

    @Test
    public void
    whenAServerIsRunningStatusPageCanBeRequested() throws IOException {
        String statusPageXml = load(String.format("http://localhost:%d/info/status", server.getBase().getPort()));

        assertThat(statusPageXml, containsString("test-tucker"));
    }

    @Test
    public void
    whenAServerIsRunningStatusPageCanBeRequestedAsJSONP() throws IOException {
        String callbackFunction = "test" + System.currentTimeMillis();
        String statusPageJavascript = load(String.format("http://localhost:%d/info/status?callback=%s", server.getBase().getPort(), callbackFunction));

        assertThat(statusPageJavascript, containsString("test-tucker"));
        assertThat(statusPageJavascript, startsWith(callbackFunction + "({"));
        assertThat(statusPageJavascript, endsWith("})"));
    }

    private String load(String url) throws IOException {
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(new URL(url).openStream()));

        String line = reader.readLine();
        StringBuffer content = new StringBuffer();

        while (line != null) {
            content.append(line);
            line = reader.readLine();
        }

        return content.toString();
    }
}
