package com.timgroup.tucker.info.component.pending;

import java.util.concurrent.atomic.AtomicBoolean;

import com.timgroup.tucker.info.Health;
import com.timgroup.tucker.info.HealthStateChangeCallback;

public final class PendingHealth implements Health {
    private final Health underlying;
    private final HealthStateChangeCallback callback;
    private final AtomicBoolean previouslyHealthy = new AtomicBoolean(false);

    private PendingHealth(Health underlying, HealthStateChangeCallback callback) {
        this.underlying = underlying;
        this.callback = callback;
    }

    public static PendingHealth pending(Health underlying, HealthStateChangeCallback callback) {
        return new PendingHealth(underlying, callback);
    }

    public static PendingHealth pending(Health underlying) {
        return pending(underlying, HealthStateChangeCallback.NOOP);
    }

    @Override
    public State get() {
        if (underlying.get() == State.ill) {
            if (previouslyHealthy.getAndSet(false)) {
                callback.healthStateChanged(State.ill);
            }
        }
        else if (!previouslyHealthy.getAndSet(true)) {
            callback.healthStateChanged(State.healthy);
        }
        return State.healthy;
    }
}
