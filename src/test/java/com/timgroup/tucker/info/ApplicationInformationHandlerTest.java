package com.timgroup.tucker.info;

import com.codahale.metrics.MetricRegistry;
import com.timgroup.metrics.Metrics;
import com.timgroup.tucker.info.component.VersionComponent;
import com.timgroup.tucker.info.status.StatusPageGenerator;
import io.prometheus.client.exporter.common.TextFormat;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static com.timgroup.tucker.info.Stoppable.State.safe;
import static com.timgroup.tucker.info.Stoppable.State.unwise;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
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

    private final ApplicationInformationHandler handler = new ApplicationInformationHandler(new StatusPageGenerator("appId", version), stoppable, health, new Metrics().getMetricWriter());

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
        ApplicationInformationHandler handler = new ApplicationInformationHandler(new StatusPageGenerator("appId", version), stoppable, Health.ALWAYS_HEALTHY,  new Metrics().getMetricWriter());

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

        ApplicationInformationHandler handler = new ApplicationInformationHandler(new StatusPageGenerator("appId", version), stoppable, alwaysIll,  new Metrics().getMetricWriter());

        final ByteArrayOutputStream responseContent = new ByteArrayOutputStream();

        final WebResponse response = mock(WebResponse.class);
        when(response.respond("text/plain", "UTF-8")).thenReturn(responseContent);

        handler.handle("/health", response);

        verify(response).respond("text/plain", "UTF-8");
        assertEquals("ill", responseContent.toString());
    }

    @Test
    public void when_application_is_healthy_returns_ready() throws Exception {
        ApplicationInformationHandler handler = new ApplicationInformationHandler(new StatusPageGenerator("appId", version), stoppable, Health.ALWAYS_HEALTHY,  new Metrics().getMetricWriter());

        final WebResponse response = mock(WebResponse.class);

        handler.handle("/ready", response);

        verify(response).respond(204);
    }

    @Test
    public void when_application_is_not_healthy_returns_unready() throws Exception {
        Health alwaysIll = Health.healthyWhen(() -> false);

        ApplicationInformationHandler handler = new ApplicationInformationHandler(new StatusPageGenerator("appId", version), stoppable, alwaysIll,  new Metrics().getMetricWriter());

        final WebResponse response = mock(WebResponse.class);

        handler.handle("/ready", response);

        verify(response).respond(503);
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

    @Test
    public void prints_all_metrics_in_plain_text() throws IOException {
        Metrics metrics = new Metrics();
        MetricRegistry metricRegistry =metrics.getMetricRegistry();
        metricRegistry.counter("my_first_metric").inc();
        metricRegistry.histogram("my_first_histogram").update(42);

        ApplicationInformationHandler handler = new ApplicationInformationHandler(new StatusPageGenerator("appId", version), stoppable, health, metrics.getMetricWriter());

        final ByteArrayOutputStream responseContent = new ByteArrayOutputStream();

        final WebResponse response = mock(WebResponse.class);
        when(response.respond(TextFormat.CONTENT_TYPE_004, "UTF-8")).thenReturn(responseContent);

        handler.handle("/metrics", response);

        assertThat(responseContent.toString(), allOf(
                containsString("my_first_histogram_count 1.0\n"),
                containsString("\nmy_first_histogram{quantile=\"0.999\",} 42.0\n"),
                containsString("my_first_metric 1.0\n")
        ));
    }
}
