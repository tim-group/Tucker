package com.timgroup.tucker.info.async;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.concurrent.TimeUnit;

public final class AsyncSettings {
    final Clock clock;
    final long repeat;
    final TimeUnit repeatTimeUnit;
    final StatusUpdated statusUpdateHook;
    final long stalenessLimit;
    final TimeUnit stalenessTimeUnit;

    private AsyncSettings(Clock clock, long repeat, TimeUnit repeatTimeUnit, StatusUpdated statusUpdateHook,
            long stalenessLimit, TimeUnit stalenessTimeUnit) {
        this.clock = clock;
        this.repeat = repeat;
        this.repeatTimeUnit = repeatTimeUnit;
        this.statusUpdateHook = statusUpdateHook;
        this.stalenessLimit = stalenessLimit;
        this.stalenessTimeUnit = stalenessTimeUnit;
    }

    public static AsyncSettings settings() {
        return new AsyncSettings(new Clock.SystemClock(), 30, SECONDS, StatusUpdated.NOOP, 5, MINUTES);
    }

    public AsyncSettings withClock(Clock clock) {
        return new AsyncSettings(clock, repeat, repeatTimeUnit, statusUpdateHook, stalenessLimit, stalenessTimeUnit);
    }

    public AsyncSettings withRepeatSchedule(long time, TimeUnit units) {
        return new AsyncSettings(clock, time, units, statusUpdateHook, stalenessLimit, stalenessTimeUnit);
    }

    public AsyncSettings withUpdateHook(StatusUpdated statusUpdated) {
        return new AsyncSettings(clock, repeat, repeatTimeUnit, statusUpdated, stalenessLimit, stalenessTimeUnit);
    }

    public AsyncSettings withStalenessLimit(long time, TimeUnit units) {
        return new AsyncSettings(clock, repeat, repeatTimeUnit, statusUpdateHook, time, units);
    }
}