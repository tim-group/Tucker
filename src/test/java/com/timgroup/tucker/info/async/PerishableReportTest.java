package com.timgroup.tucker.info.async;

import com.timgroup.tucker.info.Report;
import org.junit.Test;

import java.time.Duration;
import java.time.Instant;

import static com.timgroup.tucker.info.Status.*;
import static org.junit.Assert.assertEquals;


public final class PerishableReportTest {

    private final ManualClock clock = ManualClock.initiallyAt(Instant.now());

    @Test
    public void upgradesUnderlyingHappyReportToWarningIfStale() {
        PerishableReport report = new PerishableReport(new Report(OK, "all is well"), clock, Duration.ofSeconds(1L));

        assertEquals(OK, report.getPotentiallyStaleReport().getStatus());
        clock.bump(Duration.ofSeconds(2));
        assertEquals(WARNING, report.getPotentiallyStaleReport().getStatus());
    }

    @Test
    public void doesNotDowngradeUnderlyingReportStatusIfStale() {
        PerishableReport report = new PerishableReport(new Report(CRITICAL, "oh no"), clock, Duration.ofSeconds(1L));

        assertEquals(CRITICAL, report.getPotentiallyStaleReport().getStatus());
        clock.bump(Duration.ofSeconds(2));
        assertEquals(CRITICAL, report.getPotentiallyStaleReport().getStatus());
    }

}