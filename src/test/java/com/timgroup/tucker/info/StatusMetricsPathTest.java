package com.timgroup.tucker.info;

import com.timgroup.metrics.MetricsWriter;
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

public class StatusMetricsPathTest {
    private final StatusPageGenerator statusPage = new StatusPageGenerator("testing", new VersionComponent() {
        @Override public Report getReport() {
            return new Report(Status.INFO, "1.0.0");
        }
    });
    private final ApplicationInformationHandler handler = new ApplicationInformationHandler(statusPage, null, null, (MetricsWriter) null);

    @Test
    public void status_page_components_are_metrics() throws IOException {
        statusPage.addComponent(new ConstantValueComponent("test-comp", "Test Component:", Status.OK, "unused"));

        StringWebResponse response = new StringWebResponse();
        handler.handle("/status.metrics", response);

        Map<String, Double> metrics = extractMetrics(response.bodyString());

        assertThat(metrics, hasEntry("tucker_component_status{component=\"test-comp\",status=\"ok\",}", 1.0));
        assertThat(metrics, hasEntry("tucker_component_status{component=\"test-comp\",status=\"info\",}", 0.0));
        assertThat(metrics, hasEntry("tucker_component_status{component=\"test-comp\",status=\"warning\",}", 0.0));
        assertThat(metrics, hasEntry("tucker_component_status{component=\"test-comp\",status=\"critical\",}", 0.0));
    }

    private Map<String, Double> extractMetrics(String output) {
        return Stream.of(output.split("\n"))
                .filter(line -> !(line.matches("^#.*") || line.trim().equals("")))
                .map(line -> line.split("\\s+"))
                .collect(Collectors.toMap(parts -> parts[0], parts -> Double.parseDouble(parts[1])));
    }
}
