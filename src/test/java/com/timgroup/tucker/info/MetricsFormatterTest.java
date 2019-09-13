package com.timgroup.tucker.info;

import com.codahale.metrics.MetricRegistry;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class MetricsFormatterTest {

    @Test public void metrics_are_printed() throws IOException {
        MetricRegistry registry = new MetricRegistry();
        registry.gauge("uptime", () -> () -> 42L);
        registry.counter("thingy").inc();
        registry.counter("uther_thingy").inc();
        registry.histogram("histo").update(27);
        registry.histogram("ahisto").update(26);

        StringWriter output = new StringWriter();
        new MetricsFormatter(registry).format(output);

        List<String> keysInOutputOrder = Stream.of(output.toString().split("\n"))
                .filter(line -> !line.matches("^#.*"))
                .map(line -> line.split("\\s+")[0])
                .sorted()
                .collect(toList());

        assertThat(keysInOutputOrder, equalTo(asList(
                "ahisto_count", "ahisto{quantile=\"0.5\",}", "ahisto{quantile=\"0.75\",}", "ahisto{quantile=\"0.95\",}", "ahisto{quantile=\"0.98\",}", "ahisto{quantile=\"0.99\",}", "ahisto{quantile=\"0.999\",}",
                "histo_count", "histo{quantile=\"0.5\",}", "histo{quantile=\"0.75\",}", "histo{quantile=\"0.95\",}", "histo{quantile=\"0.98\",}", "histo{quantile=\"0.99\",}", "histo{quantile=\"0.999\",}",
                "thingy",
                "uptime",
                "uther_thingy"
        )));
    }
}
