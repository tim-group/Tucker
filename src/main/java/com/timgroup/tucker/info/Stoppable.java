package com.timgroup.tucker.info;

import java.util.function.Supplier;

public interface Stoppable extends Supplier<Stoppable.State> {
    public static final Stoppable ALWAYS_STOPPABLE = () -> State.safe;

    public enum State { safe, unwise }

    State get();
}
