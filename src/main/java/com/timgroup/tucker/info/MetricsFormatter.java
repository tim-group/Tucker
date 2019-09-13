package com.timgroup.tucker.info;

import com.codahale.metrics.MetricRegistry;
import io.prometheus.client.dropwizard.DropwizardExports;
import io.prometheus.client.exporter.common.TextFormat;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;

class MetricsFormatter {
    private final MetricRegistry metricRegistry;

    public static final String CONTENT_TYPE = TextFormat.CONTENT_TYPE_004;

    public MetricsFormatter(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    void format(Writer output) throws IOException {
        DropwizardExports exports = new DropwizardExports(metricRegistry);
        TextFormat.write004(output, Collections.enumeration(exports.collect()));
        output.flush();
    }
}
