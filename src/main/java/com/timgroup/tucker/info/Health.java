package com.timgroup.tucker.info;

public interface Health {
    public static final Health ALWAYS_HEALTHY = new Health(){
        @Override public State get() { return State.healthy; }
    };

    public enum State { healthy, ill }

    State get();
}
