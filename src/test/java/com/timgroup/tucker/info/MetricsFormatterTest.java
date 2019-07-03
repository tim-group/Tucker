package com.timgroup.tucker.info;

import com.codahale.metrics.MetricRegistry;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class MetricsFormatterTest {

    @Test public void metrics_are_printed_in_name_order() {
        MetricRegistry registry = new MetricRegistry();
        registry.gauge("uptime", () -> () -> 42L);
        registry.counter("thingy").inc();
        registry.counter("uther_thingy").inc();
        registry.histogram("histo").update(27);
        registry.histogram("ahisto").update(26);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        new MetricsFormatter(new PrintStream(outputStream), registry).format();

        List<String> keysInOutputOrder = Stream.of(outputStream.toString().split("\n"))
                .map(line -> line.split("\\s+")[0])
                .collect(toList());

        assertThat(keysInOutputOrder, equalTo(asList(
                "ahisto.count", "ahisto.max", "ahisto.mean", "ahisto.min", "ahisto.stddev", "ahisto.p50", "ahisto.p75", "ahisto.p95", "ahisto.p98", "ahisto.p99", "ahisto.p999",
                "histo.count", "histo.max", "histo.mean", "histo.min", "histo.stddev", "histo.p50", "histo.p75", "histo.p95", "histo.p98", "histo.p99", "histo.p999",
                "thingy.count",
                "uptime",
                "uther_thingy.count"
        )));
    }
}
