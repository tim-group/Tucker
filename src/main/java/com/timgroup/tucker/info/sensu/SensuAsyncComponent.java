package com.timgroup.tucker.info.sensu;

import com.timgroup.tucker.info.Component;
import com.timgroup.tucker.info.Status;
import com.timgroup.tucker.info.async.AsyncComponent;
import com.timgroup.tucker.info.async.AsyncSettings;
import com.timgroup.tucker.info.async.StatusUpdated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.net.Socket;
import java.time.Duration;
import java.util.Collection;

import static com.timgroup.tucker.info.async.BroadcastingStatusUpdated.broadcastingTo;
import static java.util.stream.Collectors.joining;

public class SensuAsyncComponent {
    private static final Logger LOGGER = LoggerFactory.getLogger(SensuAsyncComponent.class);
    private static final int DefaultPort = 3030;

    private SensuAsyncComponent() {}

    public static AsyncComponent wrapping(Component component, AsyncSettings asyncSettings, Collection<String> slackChannels) {
        return wrapping(component, asyncSettings, slackChannels, DefaultPort);
    }

    public static AsyncComponent wrapping(Component component, AsyncSettings asyncSettings, Collection<String> slackChannels, int localPort) {
        return wrapping(AsyncComponent.wrapping(component, asyncSettings), slackChannels, localPort);
    }

    public static AsyncComponent wrapping(AsyncComponent component, Collection<String> slackChannels) {
        return wrapping(component, slackChannels, DefaultPort);
    }

    public static AsyncComponent wrapping(AsyncComponent component, Collection<String> slackChannels, int localPort) {
        return component.withUpdatedSettings(settings -> settings.withUpdateHook(broadcastingTo(
                sensuReporterFor(localPort, component.getId(), settings.stalenessLimit, slackChannels),
                settings.statusUpdateHook)
        ));
    }

    private static StatusUpdated sensuReporterFor(int localPort, String componentId, Duration stalenessLimit, Collection<String> slackChannels) {
        return report -> {
            try (Socket sensuSocket = new Socket("localhost", localPort);
                 PrintWriter out = new PrintWriter(sensuSocket.getOutputStream())) {

                String json =
                        "{\n" +
                                "\"name\": \"" + componentId + "\",\n" +
                                "\"output\": \"" + report.getValue() + "\",\n" +
                                "\"status\": " + sensuStatusFor(report.getStatus()) + ",\n" +
                                "\"ttl\": " + stalenessLimit.getSeconds() + ",\n" +
                                "\"slack\": { \"channels\": " + slackChannels.stream().map(c -> "\"" + c + "\"").collect(joining(", ", "[", "]")) + " }" +
                                "}";

                out.println(json);
            } catch (Exception e) {
                LOGGER.warn("Failed to report to sensu", e);
            }
        };
    }

    private static int sensuStatusFor(Status status) {
        if (status == Status.CRITICAL) {
            return 2;
        } else if (status == Status.WARNING) {
            return 1;
        } else if (status == Status.OK) {
            return 0;
        } else if (status == Status.INFO) {
            return 0;
        } else {
            throw new RuntimeException("Don't know how to represent " + status + " in sensu.");
        }
    }
}
