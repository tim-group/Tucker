package com.timgroup.tucker.info;

import com.codahale.metrics.MetricRegistry;
import io.prometheus.client.dropwizard.DropwizardExports;
import io.prometheus.client.exporter.common.TextFormat;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;

public class LegacyMetricsWriter implements MetricsWriter {

    private final MetricRegistry metricRegistry;

    public LegacyMetricsWriter(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    @Override
    public void writeMetrics(Writer writer) throws IOException {
        DropwizardExports exports = new DropwizardExports(metricRegistry);
        TextFormat.write004(writer, Collections.enumeration(exports.collect()));
    }
}
