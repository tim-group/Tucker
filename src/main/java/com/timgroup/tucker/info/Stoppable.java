package com.timgroup.tucker.info;

import java.util.function.Supplier;

public interface Stoppable extends Supplier<Stoppable.State> {
    public static final Stoppable ALWAYS_STOPPABLE = () -> State.safe;

    public enum State { safe, unwise }

    State get();

    default Stoppable and(Stoppable other) {
        return () -> get() == State.safe && other.get() == State.safe ? State.safe : State.unwise;
    }
}
