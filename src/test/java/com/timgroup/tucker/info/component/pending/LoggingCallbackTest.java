package com.timgroup.tucker.info.component.pending;

import java.lang.reflect.Field;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.SimpleLoggerFactory;

import com.timgroup.tucker.info.Component;
import com.timgroup.tucker.info.Report;
import com.timgroup.tucker.info.Status;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class LoggingCallbackTest {
    private LoggingCallback callback;
    private Logger logger = mock(Logger.class);

    @Rule public ExternalResource injectLogger = new ExternalResource() {
        private Map<String, Logger> loggerMap;
        private final String loggerName = PendingComponent.class.getName();

        @Override protected void before() throws Throwable {
            initLoggerMap();
            loggerMap.put(loggerName, logger);
            callback = new LoggingCallback();
        }

        @Override protected void after() {
            if (loggerMap != null) {
                loggerMap.remove(loggerName);
            }
        }

        @SuppressWarnings("unchecked") private void initLoggerMap() throws NoSuchFieldException, IllegalAccessException {
            Field field = SimpleLoggerFactory.class.getDeclaredField("loggerMap");
            field.setAccessible(true);
            loggerMap = (Map<String, Logger>) field.get(LoggerFactory.getILoggerFactory());
        }
    };

    @Test public void json_escaped_in_values() throws Exception {
        Component component = new Component("id", "label") {
            @Override public Report getReport() {
                throw new IllegalStateException();
            }
        };
        Report previous = new Report(Status.OK, "\"with quotes\"");
        Report current = new Report(Status.OK, "with \\ backslash");
        callback.stateChanged(component, previous, current);
        verify(logger).info(Mockito.anyString(), Mockito.eq("id"), Mockito.eq("label"),
                Mockito.eq("OK"), Mockito.eq("\\\"with quotes\\\""),
                Mockito.eq("OK"), Mockito.eq("with \\\\ backslash"));
    }
}
