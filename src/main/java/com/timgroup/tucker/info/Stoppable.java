package com.timgroup.tucker.info;

public interface Stoppable {
    public static final Stoppable ALWAYS_STOPPABLE = new Stoppable(){
        @Override public State get() { return State.safe; }
    };

    public enum State { safe, unwise }

    State get();
}
