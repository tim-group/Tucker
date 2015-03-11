package com.timgroup.tucker.info.log;

import com.timgroup.tucker.info.ComponentStateChangeCallback;
import com.timgroup.tucker.info.component.pending.PendingComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.timgroup.tucker.info.Component;
import com.timgroup.tucker.info.Report;

public final class LoggingCallback implements ComponentStateChangeCallback {

    private static final Logger LOGGER = LoggerFactory.getLogger(PendingComponent.class);

    private static final String LOG_EVENT_FORMAT = "{" +
            "\"eventType\": \"ComponentStateChange\", " +
            "\"event\": {" +
                "\"id\": \"{}\", " +
                "\"label\": \"{}\", " +
                "\"previousStatus\": \"{}\", " +
                "\"previousValue\": \"{}\", " +
                "\"currentStatus\": \"{}\", " +
                "\"currentValue\": \"{}\"" +
            "}}";

    @Override
    public void stateChanged(Component component, Report previous, Report current) {
        LOGGER.info(LOG_EVENT_FORMAT,
            jsonEscape(component.getId()), jsonEscape(component.getLabel()),
            jsonEscape(previous.getStatus()), jsonEscape(previous.getValue()),
            jsonEscape(current.getStatus()), jsonEscape(current.getValue()));
    }

    private static String jsonEscape(Object obj) {
        CharSequence in = String.valueOf(obj);
        StringBuilder out = new StringBuilder(in.length());
        for (int i = 0; i < in.length(); i++) {
            char c = in.charAt(i);
            switch (c) {
            case '\\':
            case '\"':
                out.append('\\');
                // fallthru
            default:
                out.append(c);
            }
        }
        return out.toString();
    }
}