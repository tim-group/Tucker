package com.timgroup.tucker.info;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.time.Duration;

public final class StartupTimer {

    private final Logger logger;
    private final RuntimeMXBean runtimeMXBean;
    private final Health health;
    private final Duration pollingInterval;
    private volatile boolean stopRequested;

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
        Thread thread = new Thread(this::checkAndLogHealth);
        thread.setName("StartupTimer");
        thread.setDaemon(true);
        thread.start();
    }

    public void stop() {
        stopRequested = true;
    }

    private void checkAndLogHealth() {
        try {
            while (health.get() != Health.State.healthy) {
                if (stopRequested)
                    return;
                try {
                    Thread.sleep(pollingInterval.toMillis());
                } catch (InterruptedException ignored) {
                }
            }
        } catch (Throwable t) {
            logger.error("Error occurred whilst waiting for healthy status", t);
            throw t;
        }
        long jvmUptimeSeconds = (long) Math.ceil((double) runtimeMXBean.getUptime() / 1000.0);
        logger.info("{\"eventType\":\"JvmUptimeAtFirstHealthy\",\"event\":{\"durationSeconds\":" + jvmUptimeSeconds + "},\"retention_period\":\"long\"}");
    }
}
