package com.timgroup.tucker.info;

import static com.timgroup.tucker.info.Health.State.healthy;
import static com.timgroup.tucker.info.Health.State.ill;

public class CombinedHealth implements Health {
    private final Health[] healths;

    public CombinedHealth(Health... healths) {
        this.healths = healths;
    }

    @Override
    public State get() {
        for (Health health : healths) {
            if (ill == health.get()) {
                return ill;
            }
        }
        return healthy;
    }
}
