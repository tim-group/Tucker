package com.timgroup.tucker.info;

import com.codahale.metrics.MetricRegistry;
import com.timgroup.tucker.info.status.StatusPageGenerator;
import io.prometheus.client.Collector;
import io.prometheus.client.dropwizard.DropwizardExports;
import io.prometheus.client.exporter.common.TextFormat;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

class MetricsFormatter {
    private final MetricRegistry metricRegistry;
    private final StatusPageGenerator statusPage;

    public static final String CONTENT_TYPE = TextFormat.CONTENT_TYPE_004;

    public MetricsFormatter(MetricRegistry metricRegistry, StatusPageGenerator statusPage) {
        this.metricRegistry = metricRegistry;
        this.statusPage = statusPage;
    }

    void format(Writer output) throws IOException {
        List<Collector.MetricFamilySamples> samples = new LinkedList<>();
        dropwizardMetrics(samples);
        statusPageMetrics(samples);
        TextFormat.write004(output, Collections.enumeration(samples));
        output.flush();
    }

    private void statusPageMetrics(List<Collector.MetricFamilySamples> samples) {
        samples.add(statusPage.getApplicationReport().convertToMetrics());
    }

    private void dropwizardMetrics(List<Collector.MetricFamilySamples> samples) {
        DropwizardExports exports = new DropwizardExports(metricRegistry);
        samples.addAll(exports.collect());
    }
}
