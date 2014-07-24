package com.timgroup.tucker.info.component;

import com.timgroup.tucker.info.Component;
import com.timgroup.tucker.info.Report;
import com.timgroup.tucker.info.Status;

import static java.lang.String.format;

public class MemoryUsageComponent extends Component {
    private static final double PERCENTAGE = 100.0;
    private static final double DEFAULT_CRITICAL_THRESHOLD = 95.0;
    private static final double DEFAULT_WARNING_THRESHOLD = 80.0;

    private final double warningThreshold;
    private final double criticalThreshold;

    public MemoryUsageComponent() {
        this(DEFAULT_WARNING_THRESHOLD, DEFAULT_CRITICAL_THRESHOLD);
    }

    /**
     * @param warningThresholdPercentage (between 0.0 and 100.0)
     * @param criticalThresholdPercentage (between 0.0 and 100.0)
     */
    public MemoryUsageComponent(double warningThresholdPercentage, double criticalThresholdPercentage) {
        super("memoryUsage", "JVM Memory Usage");
        this.warningThreshold = warningThresholdPercentage;
        this.criticalThreshold = criticalThresholdPercentage;
    }

    @Override
    public Report getReport() {
        Runtime runtime = Runtime.getRuntime();
        long free = runtime.freeMemory();
        long total = runtime.totalMemory();
        double percentageUsed = PERCENTAGE * (total - free) / total;

        return new Report(statusGiven(percentageUsed), format("%.0f%%", percentageUsed));
    }

    private Status statusGiven(double percentageUsed) {
        if (percentageUsed > criticalThreshold) {
            return Status.CRITICAL;
        }
        if (percentageUsed > warningThreshold) {
            return Status.WARNING;
        }
        return Status.OK;
    }

}
