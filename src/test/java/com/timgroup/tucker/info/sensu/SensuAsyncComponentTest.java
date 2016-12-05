package com.timgroup.tucker.info.sensu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.timgroup.tucker.info.Report;
import com.timgroup.tucker.info.async.AsyncComponent;
import com.timgroup.tucker.info.component.SimpleValueComponent;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;

import static com.timgroup.tucker.info.Status.CRITICAL;
import static com.timgroup.tucker.info.Status.INFO;
import static com.timgroup.tucker.info.Status.OK;
import static com.timgroup.tucker.info.Status.WARNING;
import static com.timgroup.tucker.info.async.AsyncSettings.settings;
import static com.timgroup.tucker.info.sensu.SensuAsyncComponent.wrapping;
import static com.youdevise.testutils.matchers.json.JsonEquivalenceMatchers.equivalentTo;
import static java.time.Duration.ofSeconds;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class SensuAsyncComponentTest {
    @Rule public final FakeSensuClient fakeSensuClient = new FakeSensuClient();
    public final SimpleValueComponent component = new SimpleValueComponent("component-id", "component label");

    @Test public void
    notifies_sensu_when_component_runs() throws IOException, InterruptedException {
        component.updateValue(OK, "It worked");
        wrapping(component, settings().withStalenessLimit(ofSeconds(54)), emptyList(), fakeSensuClient.port()).update();

        assertThat(fakeSensuClient.nextResult(), equivalentTo("{" +
                    "'name': 'componentid', " +
                    "'output': 'It worked', " +
                    "'status': 0, " +
                    "'ttl': 54, " +
                    "'slack': {'channels': []}" +
                "}"));
    }

    @Test public void
    check_output_must_be_a_string_in_order_to_send_to_sensu_server() {
        Object value = new Object();
        component.updateValue(OK, value);
        wrapping(component, settings().withStalenessLimit(ofSeconds(54)), emptyList(), fakeSensuClient.port()).update();

        assertThat(fakeSensuClient.nextResult(), equivalentTo("{" +
                "'name': 'componentid', " +
                String.format("'output': '%s', ", value.toString()) +
                "'status': 0, " +
                "'ttl': 54, " +
                "'slack': {'channels': []}" +
                "}"));

    }

    @Test public void
    check_name_must_be_a_string_and_cannot_contain_spaces_or_special_characters() {
        SimpleValueComponent component = new SimpleValueComponent("/-+!@#$%^&())\";:[]{}\\ |wetyk 678dfgh", "component label");
        component.updateValue(OK, "It worked");
        wrapping(component, settings().withStalenessLimit(ofSeconds(54)), emptyList(), fakeSensuClient.port()).update();

        assertThat(fakeSensuClient.nextResult(), equivalentTo("{" +
                "'name': '_wetyk_678dfgh', " +
                "'output': 'It worked', " +
                "'status': 0, " +
                "'ttl': 54, " +
                "'slack': {'channels': []}" +
                "}"));
    }

    @Test public void
    maps_component_status_to_sensu_outputs() throws IOException, InterruptedException {
        AsyncComponent sensuNotifyingComponent = wrapping(component, settings(), emptyList(), fakeSensuClient.port());

        component.updateValue(OK, "");
        sensuNotifyingComponent.update();
        component.updateValue(INFO, "");
        sensuNotifyingComponent.update();
        component.updateValue(WARNING, "");
        sensuNotifyingComponent.update();
        component.updateValue(CRITICAL, "");
        sensuNotifyingComponent.update();

        assertThat(fakeSensuClient.nextResult(), containsString("\"status\":0"));
        assertThat(fakeSensuClient.nextResult(), containsString("\"status\":0"));
        assertThat(fakeSensuClient.nextResult(), containsString("\"status\":1"));
        assertThat(fakeSensuClient.nextResult(), containsString("\"status\":2"));
    }

    @Test public void
    can_specify_slack_channels() {
        component.updateValue(OK, "It worked");
        wrapping(component,
                settings(),
                asList("#channel-1", "#channel-2", "#channel-3"),
                fakeSensuClient.port()).update();

        assertThat(fakeSensuClient.nextResult(), containsString("\"channels\":[\"#channel-1\",\"#channel-2\",\"#channel-3\"]"));
    }

    @Test public void
    pre_existing_update_handler_still_invoked() {
        AtomicReference<Report> handled = new AtomicReference<>();
        component.updateValue(OK, "");
        wrapping(component, settings().withUpdateHook(handled::set), emptyList(), fakeSensuClient.port()).update();

        assertThat(handled.get(), is(new Report(OK, "")));
    }

    @Test public void
    when_other_update_handler_fails_sensu_still_notified() {
        component.updateValue(OK, "");
        wrapping(component, settings().withUpdateHook(report -> { throw new RuntimeException("fail"); }), emptyList(), fakeSensuClient.port()).update();

        assertThat(fakeSensuClient.nextResult(), not(nullValue()));
    }

    @Test public void
    when_sensu_not_available_still_runs_update_hook() {
        AtomicReference<Report> handled = new AtomicReference<>();
        component.updateValue(OK, "");
        wrapping(component, settings().withUpdateHook(handled::set), emptyList(), 1234).update();

        assertThat(handled.get(), is(new Report(OK, "")));
    }

    private static class FakeSensuClient extends ExternalResource {
        private BlockingQueue<String> receivedSensuResults = new LinkedBlockingQueue<>();
        private ExecutorService executor;
        private ServerSocket serverSocket;

        @Override
        protected void before() throws Throwable {
            serverSocket = new ServerSocket();
            serverSocket.bind(null);
            executor = Executors.newCachedThreadPool();
            executor.submit(() -> {
                while (true) {
                    try (Socket accept = serverSocket.accept();
                         BufferedReader in = new BufferedReader(new InputStreamReader(accept.getInputStream()))) {
                        receivedSensuResults.put(in.lines().collect(joining("\n")));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }

        @Override
        protected void after() {
            try {
                serverSocket.close();
            } catch (IOException e) {}
            executor.shutdownNow();
        }

        public int port() {
            return serverSocket.getLocalPort();
        }

        public String nextResult() {
            try {
                return receivedSensuResults.poll(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}