package com.timgroup.tucker.info.component.pending;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.timgroup.tucker.info.Component;
import com.timgroup.tucker.info.Report;

final class LoggingCallback implements ComponentStateChangeCallback {

    private static final Logger LOGGER = LoggerFactory.getLogger(PendingComponent.class);

    private static final String LOG_EVENT_FORMAT = "{" +
            "\"eventType\": \"ComponentStateChange\", " +
            "\"event\": {" +
                "\"id\": \"{}\", " +
                "\"label\": \"{}\", " +
                "\"previousStatus\": \"{}\", " +
                "\"previousValue\": \"{}\", " +
                "\"currentStatus\": \"{}\", " +
                "\"currentValue\": \"{}\", " +
            "}}";

    @Override
    public void stateChanged(Component component, Report previous, Report current) {
        LOGGER.info(LOG_EVENT_FORMAT,
            component.getId(), component.getLabel(),
            previous.getStatus(), previous.getValue(),
            current.getStatus(), current.getValue());
    }

}