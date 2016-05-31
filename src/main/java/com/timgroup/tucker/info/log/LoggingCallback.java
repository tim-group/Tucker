package com.timgroup.tucker.info.log;

import com.timgroup.tucker.info.ComponentStateChangeCallback;
import com.timgroup.tucker.info.component.pending.PendingComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.timgroup.tucker.info.Component;
import com.timgroup.tucker.info.Report;

public final class LoggingCallback implements ComponentStateChangeCallback {
    private static final Logger LOGGER = LoggerFactory.getLogger(PendingComponent.class);

    @Override
    public void stateChanged(Component component, Report previous, Report current) {
        try (JsonGenerator jgen = JsonFormatter.generate(LOGGER::info)) {
            jgen.writeStartObject();
            jgen.writeStringField("eventType", "ComponentStateChange");
            jgen.writeObjectFieldStart("event");
            jgen.writeStringField("id", component.getId());
            jgen.writeStringField("label", component.getLabel());
            jgen.writeStringField("previousStatus", String.valueOf(previous.getStatus()));
            jgen.writeStringField("previousValue", String.valueOf(previous.getValue()));
            jgen.writeStringField("currentStatus", String.valueOf(current.getStatus()));
            jgen.writeStringField("currentValue", String.valueOf(current.getValue()));
            jgen.writeEndObject();
            jgen.writeEndObject();
        } catch (IOException e) {
        }
    }
}