package com.timgroup.tucker.info.async;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

final class ManualClock extends Clock {
    private Instant instant;

    static ManualClock initiallyAt(Instant instant) {
        return new ManualClock(instant);
    }

    private ManualClock(Instant instant) {
        this.instant = instant;
    }

    void bump(Duration duration) {
        instant = instant.plus(duration);
    }

    @Override
    public ZoneId getZone() {
        return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        if (zone == ZoneOffset.UTC) {
            return this;
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public Instant instant() {
        return instant;
    }
}
