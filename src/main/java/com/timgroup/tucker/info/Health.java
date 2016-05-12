package com.timgroup.tucker.info;

import java.util.function.Supplier;

public interface Health extends Supplier<Health.State> {
    public static final Health ALWAYS_HEALTHY = () -> State.healthy;

    public enum State { healthy, ill }

    State get();
}
