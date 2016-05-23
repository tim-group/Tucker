package com.timgroup.tucker.info.async;

import static com.timgroup.tucker.info.Status.WARNING;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

import com.timgroup.tucker.info.Report;

final class PerishableReport {
    private final Instant timestamp;
    private final Report report;
    
    private final Clock clock;
    private final TimeUnit stalenessTimeUnit;
    private final long stalenessLimit;
    
    public PerishableReport(Report report, Clock clock, long stalenessLimit, TimeUnit stalenessTimeUnit) {
        this.timestamp = Instant.now(clock);
        this.report = report;
        this.clock = clock;
        this.stalenessLimit = stalenessLimit;
        this.stalenessTimeUnit = stalenessTimeUnit;
    }

    public PerishableReport updatedWith(Report newReport) {
        return new PerishableReport(newReport, clock, stalenessLimit, stalenessTimeUnit);
    }

    public Report getPotentiallyStaleReport() {
        if (ChronoUnit.MILLIS.between(timestamp, Instant.now(clock)) > stalenessTimeUnit.toMillis(stalenessLimit)) {
            String message = String.format(
                "Last run at %s (over %s %s ago): %s",
                timestamp, stalenessLimit, stalenessTimeUnit.name().toLowerCase(), report.getValue());
            return new Report(WARNING, message);
        } else {
            return report;
        }
    }
}