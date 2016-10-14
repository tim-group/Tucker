package com.timgroup.tucker.info;

import java.util.Arrays;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

@FunctionalInterface
public interface Health extends Supplier<Health.State> {
    Health ALWAYS_HEALTHY = () -> State.healthy;

    enum State { healthy, ill }

    @Override
    State get();

    default Health and(Health other) {
        return () -> get() == State.healthy && other.get() == State.healthy ? State.healthy : State.ill;
    }

    static Health healthyWhen(BooleanSupplier b) {
        return () -> b.getAsBoolean() ? Health.State.healthy : Health.State.ill;
    }

    static Health combined(Health... healths) {
        return Arrays.stream(healths).reduce(Health::and).orElse(ALWAYS_HEALTHY);
    }
}
