package com.timgroup.tucker.info.sensu;

import java.net.Socket;
import java.time.Duration;
import java.util.Collection;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.timgroup.tucker.info.Component;
import com.timgroup.tucker.info.Status;
import com.timgroup.tucker.info.async.AsyncComponent;
import com.timgroup.tucker.info.async.AsyncSettings;
import com.timgroup.tucker.info.async.StatusUpdated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.timgroup.tucker.info.async.BroadcastingStatusUpdated.broadcastingTo;

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
        ObjectMapper codecs = new ObjectMapper().disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        return report -> {
            try (Socket sensuSocket = new Socket("localhost", localPort);
                 JsonGenerator gen = codecs.getFactory().createGenerator(sensuSocket.getOutputStream())) {

                gen.writeStartObject();
                gen.writeStringField("name", componentId.replace(" ", "_"));
                gen.writeObjectField("output", report.getValue());
                gen.writeNumberField("status", sensuStatusFor(report.getStatus()));
                gen.writeNumberField("ttl", stalenessLimit.getSeconds());
                gen.writeObjectFieldStart("slack");
                gen.writeArrayFieldStart("channels");
                for (String channel : slackChannels) {
                    gen.writeString(channel);
                }
                gen.writeEndArray();
                gen.writeEndObject();
                gen.writeEndObject();
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
