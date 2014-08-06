package com.timgroup.tucker.info.async;

import static com.timgroup.tucker.info.Status.WARNING;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import com.timgroup.tucker.info.Report;

final class PerishableReport {
    private final Date timestamp;
    private final Report report;
    
    private final Clock clock;
    private final TimeUnit stalenessTimeUnit;
    private final long stalenessLimit;
    
    public PerishableReport(Report report, Clock clock, long stalenessLimit, TimeUnit stalenessTimeUnit) {
        this.timestamp = clock.now();
        this.report = report;
        this.clock = clock;
        this.stalenessLimit = stalenessLimit;
        this.stalenessTimeUnit = stalenessTimeUnit;
    }

    public PerishableReport updatedWith(Report newReport) {
        return new PerishableReport(newReport, clock, stalenessLimit, stalenessTimeUnit);
    }

    public Report getPotentiallyStaleReport() {
        if ((clock.now().getTime() - timestamp.getTime()) > stalenessTimeUnit.toMillis(stalenessLimit)) {
            String message = String.format(
                "Last run at %s (over %s %s ago): %s",
                isoFormatted(timestamp), stalenessLimit, stalenessTimeUnit.name().toLowerCase(), report.getValue());
            return new Report(WARNING, message);
        } else {
            return report;
        }
    }
    
    private String isoFormatted(Date date) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        return df.format(date);
    }
}