package com.timgroup.tucker.info.sensu;

import java.net.Socket;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.timgroup.tucker.info.Component;
import com.timgroup.tucker.info.Report;
import com.timgroup.tucker.info.Status;
import com.timgroup.tucker.info.async.AsyncComponent;
import com.timgroup.tucker.info.async.AsyncComponentListener;
import com.timgroup.tucker.info.async.AsyncSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SensuAsyncComponent {
    private static final Logger LOGGER = LoggerFactory.getLogger(SensuAsyncComponent.class);
    private static final int DefaultPort = 3030;
    public static final String SPECIAL_CHARACTERS = "[\\\\!\"#$%^&()*+,./:;<=>?@\\[\\]{|}~ ]";

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
        if (hasSpecialCharactersOrSpaces(component.getId())) {
            throw new IllegalStateException("the component id: " + component.getId() + " cannot contain spaces or special characters like " + SPECIAL_CHARACTERS);
        }
        return component.withListener(new SensuNotifier(localPort, new HashSet<>(slackChannels)));
    }

    public static final class SensuNotifier implements AsyncComponentListener {
        private final int localPort;
        private final Set<String> slackChannels;

        public SensuNotifier(int localPort, Set<String> slackChannels) {
            this.localPort = localPort;
            this.slackChannels = slackChannels;
        }

        @Override
        public void accept(AsyncComponent asyncComponent, Report report) {
            try (Socket sensuSocket = new Socket("localhost", localPort);
                 JsonGenerator gen = new JsonFactory().createGenerator(sensuSocket.getOutputStream())) {

                gen.writeStartObject();
                gen.writeStringField("name", asyncComponent.getId());
                gen.writeStringField("output", report.getValue().toString());
                gen.writeNumberField("status", sensuStatusFor(report.getStatus()));
                gen.writeNumberField("ttl", asyncComponent.getStalenessLimit().getSeconds());
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
        }
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
