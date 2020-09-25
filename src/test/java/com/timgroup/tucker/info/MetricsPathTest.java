package com.timgroup.tucker.info;

import com.codahale.metrics.MetricRegistry;
import com.timgroup.metrics.Metrics;
import com.timgroup.tucker.info.component.ConstantValueComponent;
import com.timgroup.tucker.info.component.VersionComponent;
import com.timgroup.tucker.info.status.StatusPageGenerator;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;

public class MetricsPathTest {
    private final Metrics metrics = new Metrics();
    private final MetricRegistry registry = metrics.getMetricRegistry();
    private final StatusPageGenerator statusPage = new StatusPageGenerator("testing", new VersionComponent() {
        @Override public Report getReport() {
            return new Report(Status.INFO, "1.0.0");
        }
    });
    private final ApplicationInformationHandler handler = new ApplicationInformationHandler(statusPage, null, null, metrics.getMetricWriter());

    @Test
    public void dropwizard_metrics_are_printed() throws IOException {
        registry.gauge("uptime", () -> () -> 42L);
        registry.counter("thingy").inc();
        registry.counter("uther_thingy").inc();
        registry.histogram("histo").update(27);
        registry.histogram("ahisto").update(26);

        StringWebResponse response = new StringWebResponse();
        handler.handle("/metrics", response);

        Map<String, Double> metrics = extractMetrics(response.bodyString());

        assertThat(metrics, hasEntry("ahisto_count", 1.0));
        assertThat(metrics, hasEntry("ahisto{quantile=\"0.5\",}", 26.0));
        assertThat(metrics, hasEntry("ahisto{quantile=\"0.75\",}", 26.0));
        assertThat(metrics, hasEntry("ahisto{quantile=\"0.95\",}", 26.0));
        assertThat(metrics, hasEntry("ahisto{quantile=\"0.98\",}", 26.0));
        assertThat(metrics, hasEntry("ahisto{quantile=\"0.99\",}", 26.0));
        assertThat(metrics, hasEntry("ahisto{quantile=\"0.999\",}", 26.0));

        assertThat(metrics, hasEntry("histo_count", 1.0));
        assertThat(metrics, hasEntry("histo{quantile=\"0.5\",}", 27.0));
        assertThat(metrics, hasEntry("histo{quantile=\"0.75\",}", 27.0));
        assertThat(metrics, hasEntry("histo{quantile=\"0.95\",}", 27.0));
        assertThat(metrics, hasEntry("histo{quantile=\"0.98\",}", 27.0));
        assertThat(metrics, hasEntry("histo{quantile=\"0.99\",}", 27.0));
        assertThat(metrics, hasEntry("histo{quantile=\"0.999\",}", 27.0));

        assertThat(metrics, hasEntry("thingy", 1.0));
        assertThat(metrics, hasEntry("uptime", 42.0));
        assertThat(metrics, hasEntry("uther_thingy", 1.0));
    }

    private Map<String, Double> extractMetrics(String output) {
        System.out.println(output);
        return Stream.of(output.split("\n"))
                .filter(line -> !(line.matches("^#.*") || line.trim().equals("")))
                .map(line -> line.split("\\s+"))
                .collect(Collectors.toMap(parts -> parts[0], parts -> Double.parseDouble(parts[1])));
    }
}
