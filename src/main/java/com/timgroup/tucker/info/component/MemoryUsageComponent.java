package com.timgroup.tucker.info.component;

import com.timgroup.tucker.info.Component;
import com.timgroup.tucker.info.Report;
import com.timgroup.tucker.info.Status;

import static java.lang.String.format;

public class MemoryUsageComponent extends Component {
    private static final double PERCENTAGE = 100.0;

    public MemoryUsageComponent() {
        super("memoryUsage", "JVM Memory Usage");
    }

    @Override
    public Report getReport() {
        Runtime runtime = Runtime.getRuntime();
        long free = runtime.freeMemory();
        long total = runtime.totalMemory();
        double percentageUsed = PERCENTAGE * (total - free) / total;

        return new Report(Status.OK, format("%.0f%%", percentageUsed));
    }
}
