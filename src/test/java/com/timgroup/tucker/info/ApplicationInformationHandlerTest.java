package com.timgroup.tucker.info;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

import com.timgroup.tucker.info.Stoppable.State;
import com.timgroup.tucker.info.component.VersionComponent;
import com.timgroup.tucker.info.servlet.WebResponse;
import com.timgroup.tucker.info.status.StatusPageGenerator;

public class ApplicationInformationHandlerTest {

    private final Stoppable stoppable = mock(Stoppable.class);
    private final AtomicReference<String> versionString = new AtomicReference<String>("0");
    private final VersionComponent version = new VersionComponent() {
        @Override public Report getReport() {
            return new Report(Status.INFO, versionString.get());
        }
    };

    private final ApplicationInformationHandler handler = new ApplicationInformationHandler(new StatusPageGenerator("appId", version), stoppable);

    @Test
    public void responds_to_version_request_when_null_version() throws Exception {
        final ByteArrayOutputStream responseContent = new ByteArrayOutputStream();

        final WebResponse response = mock(WebResponse.class);
        when(response.respond("text/plain", "UTF-8")).thenReturn(responseContent);

        versionString.set(null);
        handler.handle("/version", response);

        verify(response).respond("text/plain", "UTF-8");
        assertEquals("", responseContent.toString());
    }

    @Test
    public void responds_to_version_request() throws Exception {
        final ByteArrayOutputStream responseContent = new ByteArrayOutputStream();

        final WebResponse response = mock(WebResponse.class);
        when(response.respond("text/plain", "UTF-8")).thenReturn(responseContent);

        versionString.set("0.0.1");
        handler.handle("/version", response);

        verify(response).respond("text/plain", "UTF-8");
        assertEquals("0.0.1", responseContent.toString());
    }

    @Test
    public void responds_to_health_request() throws Exception {
        final ByteArrayOutputStream responseContent = new ByteArrayOutputStream();

        final WebResponse response = mock(WebResponse.class);
        when(response.respond("text/plain", "UTF-8")).thenReturn(responseContent);

        handler.handle("/health", response);

        verify(response).respond("text/plain", "UTF-8");
        assertEquals("healthy", responseContent.toString());
    }

    @Test
    public void when_application_is_stoppable_returns_safe() throws Exception {

        when(stoppable.get()).thenReturn(State.safe);

        final ByteArrayOutputStream responseContent = new ByteArrayOutputStream();

        final WebResponse response = mock(WebResponse.class);
        when(response.respond("text/plain", "UTF-8")).thenReturn(responseContent);

        handler.handle("/stoppable", response);

        verify(response).respond("text/plain", "UTF-8");
        assertEquals("safe", responseContent.toString());
    }

    @Test
    public void when_application_is_not_stoppable_returns_unwise() throws Exception {

        when(stoppable.get()).thenReturn(State.unwise);

        final ByteArrayOutputStream responseContent = new ByteArrayOutputStream();

        final WebResponse response = mock(WebResponse.class);
        when(response.respond("text/plain", "UTF-8")).thenReturn(responseContent);

        handler.handle("/stoppable", response);

        verify(response).respond("text/plain", "UTF-8");
        assertEquals("unwise", responseContent.toString());
    }
}
