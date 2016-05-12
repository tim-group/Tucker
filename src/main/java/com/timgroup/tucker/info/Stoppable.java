package com.timgroup.tucker.info;

import java.util.Arrays;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

@FunctionalInterface
public interface Stoppable extends Supplier<Stoppable.State> {
    public static final Stoppable ALWAYS_STOPPABLE = () -> State.safe;

    public enum State { safe, unwise }

    @Override
    State get();

    default Stoppable and(Stoppable other) {
        return () -> get() == State.safe && other.get() == State.safe ? State.safe : State.unwise;
    }

    static Stoppable safeWhen(BooleanSupplier b) {
        return () -> b.getAsBoolean() ? State.safe : State.unwise;
    }

    static Stoppable combine(Stoppable... stoppables) {
        return Arrays.stream(stoppables).reduce(Stoppable::and).orElse(ALWAYS_STOPPABLE);
    }
}
