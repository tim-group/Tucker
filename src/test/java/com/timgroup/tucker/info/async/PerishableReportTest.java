package com.timgroup.tucker.info.async;

import com.timgroup.tucker.info.Report;
import org.junit.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.atomic.AtomicReference;

import static com.timgroup.tucker.info.Status.*;
import static org.junit.Assert.assertEquals;


public final class PerishableReportTest {

    private final AtomicReference<Instant> currentTimeFrozen = new AtomicReference<>(Instant.now());
    private final Clock clock = new Clock() {
        @Override public ZoneId getZone() { throw new UnsupportedOperationException(); }
        @Override public Clock withZone(ZoneId zone) { throw new UnsupportedOperationException(); }
        @Override public Instant instant() { return currentTimeFrozen.get(); }
    };

    @Test
    public void upgradesUnderlyingHappyReportToWarningIfStale() {
        PerishableReport report = new PerishableReport(new Report(OK, "all is well"), clock, Duration.ofSeconds(1L));

        assertEquals(OK, report.getPotentiallyStaleReport().getStatus());
        currentTimeFrozen.set(currentTimeFrozen.get().plusSeconds(2L));
        assertEquals(WARNING, report.getPotentiallyStaleReport().getStatus());
    }

    @Test
    public void doesNotDowngradeUnderlyingReportStatusIfStale() {
        PerishableReport report = new PerishableReport(new Report(CRITICAL, "oh no"), clock, Duration.ofSeconds(1L));

        assertEquals(CRITICAL, report.getPotentiallyStaleReport().getStatus());
        currentTimeFrozen.set(currentTimeFrozen.get().plusSeconds(2L));
        assertEquals(CRITICAL, report.getPotentiallyStaleReport().getStatus());
    }

}