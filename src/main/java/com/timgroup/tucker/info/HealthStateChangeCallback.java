package com.timgroup.tucker.info;

@FunctionalInterface
public interface HealthStateChangeCallback {
    void healthStateChanged(Health.State newState);

    HealthStateChangeCallback NOOP = (newState) -> {};
}
