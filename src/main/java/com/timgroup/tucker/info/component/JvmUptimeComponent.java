package com.timgroup.tucker.info.component;

import com.timgroup.tucker.info.Component;
import com.timgroup.tucker.info.Report;
import com.timgroup.tucker.info.Status;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.time.Duration;

public class JvmUptimeComponent extends Component {

    private final RuntimeMXBean runtimeMXBean;

    public JvmUptimeComponent(RuntimeMXBean runtimeMXBean) {
        super("jvmuptime", "JVM Uptime");
        this.runtimeMXBean = runtimeMXBean;
    }

    public JvmUptimeComponent() {
        this(ManagementFactory.getRuntimeMXBean());
    }

    @Override
    public Report getReport() {
        return new Report(Status.INFO, Duration.ofMillis(runtimeMXBean.getUptime()));
    }
}
