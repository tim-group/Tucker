package com.timgroup.tucker.info.async;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public final class AsyncSettings {
    final Clock clock;
    final long repeat;
    final TimeUnit repeatTimeUnit;
    final StatusUpdated statusUpdateHook;
    final Duration stalenessLimit;

    private AsyncSettings(Clock clock, long repeat, TimeUnit repeatTimeUnit, StatusUpdated statusUpdateHook,
            Duration stalenessLimit) {
        this.clock = clock;
        this.repeat = repeat;
        this.repeatTimeUnit = repeatTimeUnit;
        this.statusUpdateHook = statusUpdateHook;
        this.stalenessLimit = stalenessLimit;
    }

    public static AsyncSettings settings() {
        return new AsyncSettings(Clock.systemDefaultZone(), 30, SECONDS, StatusUpdated.NOOP, Duration.ofMinutes(5));
    }

    public AsyncSettings withClock(@SuppressWarnings("hiding") Clock clock) {
        return new AsyncSettings(clock, repeat, repeatTimeUnit, statusUpdateHook, stalenessLimit);
    }

    public AsyncSettings withRepeatSchedule(long time, TimeUnit units) {
        return new AsyncSettings(clock, time, units, statusUpdateHook, stalenessLimit);
    }

    public AsyncSettings withUpdateHook(StatusUpdated statusUpdated) {
        return new AsyncSettings(clock, repeat, repeatTimeUnit, statusUpdated, stalenessLimit);
    }

    public AsyncSettings withStalenessLimit(long time, TimeUnit units) {
        return new AsyncSettings(clock, repeat, repeatTimeUnit, statusUpdateHook, Duration.ofNanos(units.toNanos(time)));
    }

    public AsyncSettings withStalenessLimit(Duration duration) {
        return new AsyncSettings(clock, repeat, repeatTimeUnit, statusUpdateHook, duration);
    }
}
