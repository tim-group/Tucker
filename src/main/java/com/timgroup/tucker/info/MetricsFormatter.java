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

import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.BigInteger;
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

class MetricsFormatter {

    private final PrintStream output;
    private final MetricRegistry metricRegistry;

    MetricsFormatter(PrintStream output, MetricRegistry metricRegistry) {
        this.output = output;
        this.metricRegistry = metricRegistry;
    }

    void format() {
        for (Map.Entry<String, Metric> metric: metricRegistry.getMetrics().entrySet()) {
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
