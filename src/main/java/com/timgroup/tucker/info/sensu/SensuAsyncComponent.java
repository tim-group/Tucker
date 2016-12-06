package com.timgroup.tucker.info.sensu;

import java.net.Socket;
import java.time.Duration;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
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
    public static final String SPECIAL_CHARACTERS = "[\\\\!\"#$%^&()*+,./:;<=>?@\\[\\]{|}~+ ]";

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
        if (hasSpecialCharactersOrSpaces(componentId)) {
            throw new IllegalStateException("the component id cannot contain spaces or special characters like " + SPECIAL_CHARACTERS);
        }
        return report -> {
            try (Socket sensuSocket = new Socket("localhost", localPort);
                 JsonGenerator gen = new JsonFactory().createGenerator(sensuSocket.getOutputStream())) {

                gen.writeStartObject();
                gen.writeStringField("name", componentId);
                gen.writeStringField("output", report.getValue().toString());
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

    private static boolean hasSpecialCharactersOrSpaces(String componentId) {
        Pattern pattern = Pattern.compile(SPECIAL_CHARACTERS);
        Matcher matcher = pattern.matcher(componentId);
        return matcher.find();
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
