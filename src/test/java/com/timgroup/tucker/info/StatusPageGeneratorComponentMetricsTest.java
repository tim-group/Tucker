package com.timgroup.tucker.info;

import com.timgroup.tucker.info.component.VersionComponent;
import com.timgroup.tucker.info.status.StatusPageGenerator;
import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import org.junit.Test;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItems;

public class StatusPageGeneratorComponentMetricsTest {

    private final VersionComponent version = new VersionComponent() {
        @Override public Report getReport() {
            return new Report(Status.INFO, "0.0.1");
        }
    };

    @Test public void registers_component_metrics() {
        StatusPageGenerator statusPage = new StatusPageGenerator("myapp", version);
        statusPage.addComponent(new Component("AwesomeComponent", "AwesomeComponent") {
            @Override
            public Report getReport() {
                return new Report(Status.CRITICAL, "bad");
            }
        });

        final List<Collector.MetricFamilySamples> metrics = enumerationAsStream(CollectorRegistry.defaultRegistry.metricFamilySamples()).collect(Collectors.toList());

        Map<String, Double> awesomeComponentStatusValues = metrics.stream().flatMap(m ->
                        m.samples.stream().filter(a -> a.labelValues.contains("AwesomeComponent"))
        ).collect(Collectors.toMap(s -> s.labelValues.stream().filter(n -> !n.equals("AwesomeComponent")).findFirst().get(), s -> s.value));


        assertThat(metrics.stream().map(m -> m.name).collect(Collectors.toList()),
                hasItems("tucker_component_status"));

        assertThat(awesomeComponentStatusValues, hasEntry("ok", 0.0));
        assertThat(awesomeComponentStatusValues, hasEntry("info", 0.0));
        assertThat(awesomeComponentStatusValues, hasEntry("warning", 0.0));
        assertThat(awesomeComponentStatusValues, hasEntry("critical", 1.0));

    }

    private static <T> Stream<T> enumerationAsStream(Enumeration<T> e) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(
                        new Iterator<T>() {
                            public T next() {
                                return e.nextElement();
                            }
                            public boolean hasNext() {
                                return e.hasMoreElements();
                            }
                        },
                        Spliterator.ORDERED), false);
    }

}
