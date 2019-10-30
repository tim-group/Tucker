package com.timgroup.tucker.info;

import com.codahale.metrics.MetricRegistry;
import com.timgroup.tucker.info.component.ConstantValueComponent;
import com.timgroup.tucker.info.component.VersionComponent;
import com.timgroup.tucker.info.status.StatusPageGenerator;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;

public class MetricsFormatterTest {
    private final MetricRegistry registry = new MetricRegistry();
    private final StatusPageGenerator statusPage = new StatusPageGenerator("testing", new VersionComponent() {
        @Override public Report getReport() {
            return new Report(Status.INFO, "1.0.0");
        }
    });

    @Test
    public void dropwizard_metrics_are_printed() throws IOException {
        registry.gauge("uptime", () -> () -> 42L);
        registry.counter("thingy").inc();
        registry.counter("uther_thingy").inc();
        registry.histogram("histo").update(27);
        registry.histogram("ahisto").update(26);

        StringWriter output = new StringWriter();
        new MetricsFormatter(registry, statusPage).format(output);

        Map<String, Double> metrics = extractMetrics(output);

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

    @Test
    public void status_page_components_are_metrics() throws IOException {
        statusPage.addComponent(new ConstantValueComponent("test-comp", "Test Component:", Status.OK, "unused"));

        StringWriter output = new StringWriter();
        new MetricsFormatter(registry, statusPage).format(output);

        Map<String, Double> metrics = extractMetrics(output);

        assertThat(metrics, hasEntry("tucker_component_status{component=\"test-comp\",status=\"ok\",}", 1.0));
        assertThat(metrics, hasEntry("tucker_component_status{component=\"test-comp\",status=\"info\",}", 0.0));
        assertThat(metrics, hasEntry("tucker_component_status{component=\"test-comp\",status=\"warning\",}", 0.0));
        assertThat(metrics, hasEntry("tucker_component_status{component=\"test-comp\",status=\"critical\",}", 0.0));
    }

    @NotNull
    private Map<String, Double> extractMetrics(StringWriter output) {
        return Stream.of(output.toString().split("\n"))
                .filter(line -> !(line.matches("^#.*") || line.trim().equals("")))
                .map(line -> line.split("\\s+"))
                .collect(Collectors.toMap(parts -> parts[0], parts -> Double.parseDouble(parts[1])));
    }

}
