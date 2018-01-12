package com.timgroup.tucker.info;

public class StaysHealthy implements Health {
    private final Health health;
    private volatile boolean hadHealth = false;

    private StaysHealthy(Health health) {
        this.health = health;
    }

    @Override
    public Health.State get() {
        if (!hadHealth) {
            if (health.get() == Health.State.healthy) {
                hadHealth = true;
            }
        }
        return hadHealth ? Health.State.healthy : Health.State.ill;

    }

    public static Health onceHealthy(Health health) {
        return new StaysHealthy(health);
    }
}
