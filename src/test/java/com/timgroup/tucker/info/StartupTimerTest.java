package com.timgroup.tucker.info;

import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

import java.lang.management.RuntimeMXBean;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

public class StartupTimerTest {
    private final Logger logger = Mockito.mock(Logger.class);
    private final RuntimeMXBean runtimeMXBean = Mockito.mock(RuntimeMXBean.class);
    private Health.State appHealth = Health.State.ill;

    @Test public void
    logs_startup_time() throws InterruptedException {

        Duration pollingInterval = Duration.ofMillis(10L);

        new StartupTimer(logger, runtimeMXBean, () -> appHealth, pollingInterval).start();

        Mockito.verifyZeroInteractions(logger);

        Mockito.when(runtimeMXBean.getUptime()).thenReturn(999L);
        appHealth = Health.State.healthy;

        Thread.sleep(20L);

        Mockito.verify(logger).info("{\"eventType\":\"JvmUptimeAtFirstHealthy\",\"event\":{\"durationSeconds\":1},\"retention_period\":\"long\"}");
    }

    @Test public void
    logs_problem_waiting_for_healthy() throws InterruptedException {
        final AtomicBoolean hadProblem = new AtomicBoolean(false);
        RuntimeException problemException = new RuntimeException("problem");

        Health problematicHealth = () -> {
            if (hadProblem.get()) {
                throw problemException;
            }
            return Health.State.ill;
        };

        Duration pollingInterval = Duration.ofMillis(10L);

        new StartupTimer(logger, runtimeMXBean, problematicHealth, pollingInterval).start();

        hadProblem.set(true);

        Thread.sleep(20L);

        Mockito.verify(logger).error(Mockito.anyString(), Mockito.eq(problemException));
    }
}