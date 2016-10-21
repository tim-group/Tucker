package com.timgroup.tucker.info.async;

import static com.timgroup.tucker.info.Status.WARNING;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import com.timgroup.tucker.info.Report;

final class PerishableReport {
    private final Instant timestamp;
    private final Report report;
    
    private final Clock clock;
    private final Duration stalenessLimit;
    
    public PerishableReport(Report report, Clock clock, Duration stalenessLimit) {
        this.timestamp = Instant.now(clock);
        this.report = report;
        this.clock = clock;
        this.stalenessLimit = stalenessLimit;
    }

    public PerishableReport updatedWith(Report newReport) {
        return new PerishableReport(newReport, clock, stalenessLimit);
    }

    public Report getPotentiallyStaleReport() {
        if (Duration.between(timestamp, Instant.now(clock)).compareTo(stalenessLimit) > 0) {
            String message = String.format(
                "Last run at %s (over %s ago): %s",
                timestamp, stalenessLimit, report.getValue());
            return new Report(WARNING.or(report.getStatus()), message);
        } else {
            return report;
        }
    }
}