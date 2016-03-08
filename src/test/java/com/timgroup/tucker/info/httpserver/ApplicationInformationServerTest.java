package com.timgroup.tucker.info.httpserver;

import static com.timgroup.tucker.info.Health.ALWAYS_HEALTHY;
import static com.timgroup.tucker.info.Stoppable.ALWAYS_STOPPABLE;
import static com.timgroup.tucker.info.httpserver.ApplicationInformationServer.create;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.timgroup.tucker.info.component.JarVersionComponent;
import com.timgroup.tucker.info.status.StatusPageGenerator;

public class ApplicationInformationServerTest {
    StatusPageGenerator statusPage;
    ApplicationInformationServer server;


    @Before public void
    startServer() throws IOException {
        statusPage = new StatusPageGenerator("test-tucker", new JarVersionComponent(Object.class));
        server = create(8000, statusPage, ALWAYS_STOPPABLE, ALWAYS_HEALTHY);
        server.start();
    }

    @After public void
    stopServer() {
        server.stop();
    }

    @Test
    public void
    whenAServerIsRunningStatusPageCanBeRequested() throws IOException {
        String statusPageXml = load("http://localhost:8000/info/status");

        assertThat(statusPageXml, containsString("test-tucker"));
    }

    @Test
    public void
    whenAServerIsRunningStatusPageCanBeRequestedAsJSONP() throws IOException {
        String callbackFunction = "test" + System.currentTimeMillis();
        String statusPageJavascript = load("http://localhost:8000/info/status?callback=" + callbackFunction);

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
