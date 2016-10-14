package com.timgroup.tucker.info;

@FunctionalInterface
public interface ComponentStateChangeCallback {
    void stateChanged(Component component, Report previous, Report current);
}
