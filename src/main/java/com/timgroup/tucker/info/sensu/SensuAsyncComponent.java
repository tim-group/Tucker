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
import java.util.Collection;

import static com.timgroup.tucker.info.async.BroadcastingStatusUpdated.broadcastingTo;
import static java.util.stream.Collectors.joining;

public class SensuAsyncComponent {
    private static final Logger LOGGER = LoggerFactory.getLogger(SensuAsyncComponent.class);

    private SensuAsyncComponent() {}

    public static AsyncComponent wrapping(Component component, AsyncSettings asyncSettings, Collection<String> slackChannels) {
        return wrapping(component, asyncSettings, slackChannels, 3030);
    }

    public static AsyncComponent wrapping(Component component, AsyncSettings asyncSettings, Collection<String> slackChannels, int localPort) {
        StatusUpdated sensuReportingHook = report -> {
            try (Socket sensuSocket = new Socket("localhost", localPort);
                 PrintWriter out = new PrintWriter(sensuSocket.getOutputStream())) {

                String json =
                        "{\n" +
                            "\"name\": \"" + component.getId() + "\",\n" +
                            "\"output\": \"" + report.getValue() + "\",\n" +
                            "\"status\": " + sensuStatusFor(report.getStatus()) + ",\n" +
                            "\"ttl\": " + asyncSettings.stalenessLimit.getSeconds() + ",\n" +
                            "\"slack\": { \"channels\": " + slackChannels.stream().map(c -> "\"" + c + "\"").collect(joining(", ", "[", "]")) + " }" +
                        "}";

                out.println(json);
            } catch (Exception e) {
                LOGGER.warn("Failed to report to sensu", e);
            }
        };

        return AsyncComponent.wrapping(component, asyncSettings.withUpdateHook(broadcastingTo(sensuReportingHook, asyncSettings.statusUpdateHook)));
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
