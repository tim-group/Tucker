package com.timgroup.tucker.info;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metered;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricAttribute;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import com.timgroup.tucker.info.status.StatusPage;
import com.timgroup.tucker.info.status.StatusPageGenerator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.codahale.metrics.MetricAttribute.COUNT;
import static com.codahale.metrics.MetricAttribute.M15_RATE;
import static com.codahale.metrics.MetricAttribute.M1_RATE;
import static com.codahale.metrics.MetricAttribute.M5_RATE;
import static com.codahale.metrics.MetricAttribute.MAX;
import static com.codahale.metrics.MetricAttribute.MEAN;
import static com.codahale.metrics.MetricAttribute.MEAN_RATE;
import static com.codahale.metrics.MetricAttribute.MIN;
import static com.codahale.metrics.MetricAttribute.P50;
import static com.codahale.metrics.MetricAttribute.P75;
import static com.codahale.metrics.MetricAttribute.P95;
import static com.codahale.metrics.MetricAttribute.P98;
import static com.codahale.metrics.MetricAttribute.P99;
import static com.codahale.metrics.MetricAttribute.P999;
import static com.codahale.metrics.MetricAttribute.STDDEV;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;

public class ApplicationInformationHandler {

    private static final String UTF_8 = "UTF-8";

    private final Map<String, Handler> dispatch = new HashMap<>();
    private final Map<String, Handler> jsonpDispatch = new HashMap<>();

    public ApplicationInformationHandler(StatusPageGenerator statusPage, Stoppable stoppable, Health health, MetricRegistry metricRegistry) {
        dispatch.put(null, new RedirectTo("/status"));
        dispatch.put("", new RedirectTo("/status"));
        dispatch.put("/health", new HealthWriter(health));
        dispatch.put("/stoppable", new StoppableWriter(stoppable));
        dispatch.put("/version", new ComponentWriter(statusPage.getVersionComponent()));
        dispatch.put("/metrics", new MetricsWriter(metricRegistry));
        dispatch.put("/status", new StatusPageWriter(statusPage, health));
        dispatch.put("/status.json", new StatusPageJsonWriter(statusPage, health));
        dispatch.put("/status-page.dtd", new ResourceWriter(StatusPageGenerator.DTD_FILENAME, "application/xml-dtd"));
        dispatch.put("/status-page.css", new ResourceWriter(StatusPageGenerator.CSS_FILENAME, "text/css"));
        jsonpDispatch.put("/status", new StatusPageJsonWriter(statusPage, health));
        jsonpDispatch.put("/status.json", new StatusPageJsonWriter(statusPage, health));
    }

    /**
     * Deprecated as it will use a default, empty MetricRegistry. Pass your application's MetricRegistry instead.
     */
    @Deprecated
    public ApplicationInformationHandler(StatusPageGenerator statusPage, Stoppable stoppable, Health health) {
        this(statusPage, stoppable, health, new MetricRegistry());
    }

    public void handle(String path, WebResponse response) throws IOException {
        if (dispatch.containsKey(path)) {
            dispatch.get(path).handle(response);
        } else {
            response.reject(HTTP_NOT_FOUND, "try asking for .../status");
        }
    }

    public void handleJSONP(String path, String callback, WebResponse response) throws IOException {
        if (jsonpDispatch.containsKey(path)) {
            jsonpDispatch.get(path).handle(new JSONPResponse(callback, response));
        } else {
            handle(path, response);
        }
    }

    private interface Handler {
        void handle(WebResponse response) throws IOException;
    }

    private static final class JSONPResponse implements WebResponse {
        private final String callback;
        private final WebResponse underlying;

        public JSONPResponse(String callback, WebResponse underlying) {
            this.callback = callback;
            this.underlying = underlying;
        }

        @Override
        public OutputStream respond(String contentType, String characterEncoding) throws IOException {
            if (!contentType.equalsIgnoreCase("application/json")) {
                return underlying.respond(contentType, characterEncoding);
            }

            final OutputStream understream = underlying.respond("application/javascript", characterEncoding);

            understream.write(callback.getBytes(characterEncoding));
            understream.write('(');

            return new OutputStream() {
                @Override
                public void write(int b) throws IOException {
                    understream.write(b);
                }

                @Override
                public void write(byte[] b) throws IOException {
                    understream.write(b);
                }

                @Override
                public void write(byte[] b, int off, int len) throws IOException {
                    understream.write(b, off, len);
                }

                @Override
                public void close() throws IOException {
                    understream.write(')');
                    understream.close();
                }
            };
        }

        @Override
        public void reject(int status, String message) throws IOException {
            underlying.reject(status, message);
        }

        @Override
        public void redirect(String relativePath) throws IOException {
            underlying.redirect(relativePath);
        }
    }

    private static final class RedirectTo implements Handler {
        private final String targetPath;

        public RedirectTo(String targetPath) {
            this.targetPath = targetPath;
        }

        @Override public void handle(WebResponse response) throws IOException {
            response.redirect(targetPath);
        }
    }

    private static final class StatusPageWriter implements Handler {
        private final StatusPageGenerator statusPageGenerator;
        private final Health health;

        public StatusPageWriter(StatusPageGenerator statusPage, Health health) {
            this.statusPageGenerator = statusPage;
            this.health = health;
        }

        @Override public void handle(WebResponse response) throws IOException {
            try (OutputStreamWriter writer = new OutputStreamWriter(response.respond("text/xml", UTF_8), UTF_8)) {
                StatusPage report = statusPageGenerator.getApplicationReport();
                report.render(writer, health);
            }
        }
    }

    private static final class StatusPageJsonWriter implements Handler {
        private final StatusPageGenerator statusPageGenerator;
        private final Health health;

        public StatusPageJsonWriter(StatusPageGenerator statusPage, Health health) {
            this.statusPageGenerator = statusPage;
            this.health = health;
        }

        @Override public void handle(WebResponse response) throws IOException {
            try (OutputStreamWriter writer = new OutputStreamWriter(response.respond("application/json", UTF_8), UTF_8)) {
                StatusPage report = statusPageGenerator.getApplicationReport();
                report.renderJson(writer, health.get());
            }
        }
    }

    private static final class HealthWriter implements Handler {
        private Health health;

        public HealthWriter(Health health) {
            this.health = health;
        }

        @Override public void handle(WebResponse response) throws IOException {
            try (OutputStream out = response.respond("text/plain", UTF_8)) {
                out.write(health.get().name().getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    private static final class StoppableWriter implements Handler {
        private final Stoppable stoppable;

        public StoppableWriter(Stoppable stoppable) {
            this.stoppable = stoppable;
        }

        @Override public void handle(WebResponse response) throws IOException {
            try (OutputStream out = response.respond("text/plain", UTF_8)) {
                out.write(stoppable.get().name().getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    private static final class ComponentWriter implements Handler {
        private final Component component;

        public ComponentWriter(Component component) {
            this.component = component;
        }

        @Override public void handle(WebResponse response) throws IOException {
            try (OutputStream out = response.respond("text/plain", UTF_8)) {
                final Report versionReport = component.getReport();
                final String versionString = versionReport.hasValue() ? versionReport.getValue().toString() : "";
                out.write(versionString.getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    private static final class ResourceWriter implements Handler {
        private final String resourceName;
        private final String contentType;

        public ResourceWriter(String resourceName, String contentType) {
            this.resourceName = resourceName;
            this.contentType = contentType;
        }

        @Override public void handle(WebResponse response) throws IOException {
            URL resourceUri = StatusPageGenerator.class.getResource(resourceName);
            if (resourceUri == null) {
                response.reject(HTTP_NOT_FOUND, "could not find resource with name " + resourceName);
                return;
            }
            try (InputStream resource = resourceUri.openStream();
                    OutputStream out = response.respond(contentType, UTF_8)) {
                copy(resource, out);
            }
        }

        private void copy(InputStream input, OutputStream output) throws IOException {
            final byte[] buffer = new byte[1024];
            int n = 0;
            while (-1 != (n = input.read(buffer))) {
                output.write(buffer, 0, n);
            }
        }
    }

    private static final class MetricsWriter implements Handler {

        private final MetricRegistry metricRegistry;

        public MetricsWriter(MetricRegistry metricRegistry) {
            this.metricRegistry = metricRegistry;
        }

        @Override
        public void handle(WebResponse response) throws IOException {
            MetricsFormatter formatter = new MetricsFormatter(new PrintStream(response.respond("text/plain", UTF_8)));
            formatter.report(metricRegistry);
        }
    }

    private static class MetricsFormatter {

        private final PrintStream output;

        MetricsFormatter(PrintStream output) {
            this.output = output;
        }

        private void report(MetricRegistry registry) {
            for (Map.Entry<String, Metric> metric: registry.getMetrics().entrySet()) {
                if (metric.getValue() instanceof Gauge) {
                    reportGauge(metric.getKey(), (Gauge)metric.getValue());
                } else if (metric.getValue() instanceof Counter) {
                    reportCounter(metric.getKey(), (Counter)metric.getValue());
                } else if (metric.getValue() instanceof Histogram) {
                    reportHistogram(metric.getKey(), (Histogram)metric.getValue());
                } else if (metric.getValue() instanceof Meter) {
                    reportMetered(metric.getKey(), (Meter)metric.getValue());
                } else if (metric.getValue() instanceof Timer) {
                    reportTimer(metric.getKey(), (Timer)metric.getValue());
                }
            }

        }

        private void reportTimer(String name, Timer timer) {
            final Snapshot snapshot = timer.getSnapshot();
            reportSnapshot(name, snapshot);
            reportMetered(name, timer);
        }

        private void reportMetered(String name, Metered meter) {
            reportingleMetric(COUNT, name, meter.getCount());
            reportingleMetric(M1_RATE, name, meter.getOneMinuteRate());
            reportingleMetric(M5_RATE, name, meter.getFiveMinuteRate());
            reportingleMetric(M15_RATE, name, meter.getFifteenMinuteRate());
            reportingleMetric(MEAN_RATE, name, meter.getMeanRate());
        }

        private void reportHistogram(String name, Histogram histogram) {
            final Snapshot snapshot = histogram.getSnapshot();
            reportingleMetric(COUNT, name, histogram.getCount());
            reportSnapshot(name, snapshot);
        }

        private void reportSnapshot(String name, Snapshot snapshot) {
            reportingleMetric(MAX, name, snapshot.getMax());
            reportingleMetric(MEAN, name, snapshot.getMean());
            reportingleMetric(MIN, name, snapshot.getMin());
            reportingleMetric(STDDEV, name, snapshot.getStdDev());
            reportingleMetric(P50, name, snapshot.getMedian());
            reportingleMetric(P75, name, snapshot.get75thPercentile());
            reportingleMetric(P95, name, snapshot.get95thPercentile());
            reportingleMetric(P98, name, snapshot.get98thPercentile());
            reportingleMetric(P99, name, snapshot.get99thPercentile());
            reportingleMetric(P999, name, snapshot.get999thPercentile());
        }

        private void reportingleMetric(MetricAttribute type, String name, Object value) {
            reportingleMetric(prefix(name, type.getCode()), format(value));
        }

        private void reportCounter(String name, Counter counter) {
            reportingleMetric(prefix(name, COUNT.getCode()), format(counter.getCount()));
        }

        private void reportGauge(String name, Gauge gauge) {
            final String value = format(gauge.getValue());
            if (value != null) {
                reportingleMetric(prefix(name), value);
            }
        }

        private void reportingleMetric(String name, String value) {
            output.printf("%s %s%n", prefix(name) , value);
        }

        private String format(Object o) {
            if (o instanceof Float) {
                return format(((Float) o).doubleValue());
            } else if (o instanceof Double) {
                return format(((Double) o).doubleValue());
            } else if (o instanceof Byte) {
                return format(((Byte) o).longValue());
            } else if (o instanceof Short) {
                return format(((Short) o).longValue());
            } else if (o instanceof Integer) {
                return format(((Integer) o).longValue());
            } else if (o instanceof Long) {
                return format(((Long) o).longValue());
            } else if (o instanceof BigInteger) {
                return format(((BigInteger) o).doubleValue());
            } else if (o instanceof BigDecimal) {
                return format(((BigDecimal) o).doubleValue());
            } else if (o instanceof Boolean) {
                return format(((Boolean) o) ? 1 : 0);
            }
            return null;
        }

        private String prefix(String... components) {
            return MetricRegistry.name("", components);
        }

        private String format(long n) {
            return Long.toString(n);
        }

        protected String format(double v) {
            return String.format(Locale.US, "%2.2f", v);
        }


    }
}
