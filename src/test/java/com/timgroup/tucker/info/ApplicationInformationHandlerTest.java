package com.timgroup.tucker.info;

import com.timgroup.tucker.info.component.VersionComponent;
import com.timgroup.tucker.info.status.StatusPageGenerator;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static com.timgroup.tucker.info.Stoppable.State.safe;
import static com.timgroup.tucker.info.Stoppable.State.unwise;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ApplicationInformationHandlerTest {

    private final Stoppable stoppable = mock(Stoppable.class);
    private final Health health = Health.ALWAYS_HEALTHY;
    private volatile String versionString = "0";
    private final VersionComponent version = new VersionComponent() {
        @Override public Report getReport() {
            return new Report(Status.INFO, versionString);
        }
    };

    private final ApplicationInformationHandler handler = new ApplicationInformationHandler(new StatusPageGenerator("appId", version), stoppable, health);

    @Test
    public void responds_to_version_request_when_null_version() throws Exception {
        final ByteArrayOutputStream responseContent = new ByteArrayOutputStream();

        final WebResponse response = mock(WebResponse.class);
        when(response.respond("text/plain", "UTF-8")).thenReturn(responseContent);

        versionString = null;
        handler.handle("/version", response);

        verify(response).respond("text/plain", "UTF-8");
        assertEquals("", responseContent.toString());
    }

    @Test
    public void responds_to_version_request() throws Exception {
        final ByteArrayOutputStream responseContent = new ByteArrayOutputStream();

        final WebResponse response = mock(WebResponse.class);
        when(response.respond("text/plain", "UTF-8")).thenReturn(responseContent);

        versionString = "0.0.1";
        handler.handle("/version", response);

        verify(response).respond("text/plain", "UTF-8");
        assertEquals("0.0.1", responseContent.toString());
    }

    @Test
    public void when_application_is_healthy_returns_healthy() throws Exception {
        ApplicationInformationHandler handler = new ApplicationInformationHandler(new StatusPageGenerator("appId", version), stoppable, Health.ALWAYS_HEALTHY);

        final ByteArrayOutputStream responseContent = new ByteArrayOutputStream();

        final WebResponse response = mock(WebResponse.class);
        when(response.respond("text/plain", "UTF-8")).thenReturn(responseContent);

        handler.handle("/health", response);

        verify(response).respond("text/plain", "UTF-8");
        assertEquals("healthy", responseContent.toString());
    }

    @Test
    public void when_application_is_not_healthy_returns_ill() throws Exception {
        Health alwaysIll = Health.healthyWhen(() -> false);

        ApplicationInformationHandler handler = new ApplicationInformationHandler(new StatusPageGenerator("appId", version), stoppable, alwaysIll);

        final ByteArrayOutputStream responseContent = new ByteArrayOutputStream();

        final WebResponse response = mock(WebResponse.class);
        when(response.respond("text/plain", "UTF-8")).thenReturn(responseContent);

        handler.handle("/health", response);

        verify(response).respond("text/plain", "UTF-8");
        assertEquals("ill", responseContent.toString());
    }
    
    @Test
    public void when_application_is_stoppable_returns_safe() throws Exception {
        when(stoppable.get()).thenReturn(safe);

        final ByteArrayOutputStream responseContent = new ByteArrayOutputStream();

        final WebResponse response = mock(WebResponse.class);
        when(response.respond("text/plain", "UTF-8")).thenReturn(responseContent);

        handler.handle("/stoppable", response);

        verify(response).respond("text/plain", "UTF-8");
        assertEquals("safe", responseContent.toString());
    }

    @Test
    public void when_application_is_not_stoppable_returns_unwise() throws Exception {

        when(stoppable.get()).thenReturn(unwise);

        final ByteArrayOutputStream responseContent = new ByteArrayOutputStream();

        final WebResponse response = mock(WebResponse.class);
        when(response.respond("text/plain", "UTF-8")).thenReturn(responseContent);

        handler.handle("/stoppable", response);

        verify(response).respond("text/plain", "UTF-8");
        assertEquals("unwise", responseContent.toString());
    }

    @Test
    public void when_status_page_is_served_the_stream_is_closed() throws IOException {
        final OutputStream responseContent = mock(OutputStream.class);

        final WebResponse response = mock(WebResponse.class);
        when(response.respond(anyString(), anyString())).thenReturn(responseContent);

        handler.handle("/status", response);

        verify(responseContent).close();
    }
}
