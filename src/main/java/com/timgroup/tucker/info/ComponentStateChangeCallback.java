package com.timgroup.tucker.info;

public interface ComponentStateChangeCallback {
    void stateChanged(Component component, Report previous, Report current);
}