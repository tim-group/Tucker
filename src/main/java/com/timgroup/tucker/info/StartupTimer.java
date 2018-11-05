package com.timgroup.tucker.info;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class StartupTimer {

    private final Logger logger;
    private final RuntimeMXBean runtimeMXBean;
    private final Health health;
    private final Duration pollingInterval;

    public StartupTimer(Health health) {
        this(LoggerFactory.getLogger(StartupTimer.class), ManagementFactory.getRuntimeMXBean(), health, Duration.ofSeconds(1L));
    }

    StartupTimer(Logger logger, RuntimeMXBean runtimeMXBean, Health health, Duration pollingInterval) {
        this.logger = logger;
        this.runtimeMXBean = runtimeMXBean;
        this.health = health;
        this.pollingInterval = pollingInterval;
    }

    public void start() {
        Executors.newSingleThreadExecutor().execute(this::checkAndLogHealth);
    }

    private void checkAndLogHealth() {
        while (health.get() != Health.State.healthy) {
            try {
                Thread.sleep(pollingInterval.toMillis());
            } catch (InterruptedException ignored) {}
        }

        long jvmUptimeSeconds = (long) Math.ceil((double) runtimeMXBean.getUptime() / 1000.0);
        logger.info("{\"eventType\":\"JvmUptimeAtFirstHealthy\",\"event\":{\"durationSeconds\":" + jvmUptimeSeconds + "}}");
    }
}
