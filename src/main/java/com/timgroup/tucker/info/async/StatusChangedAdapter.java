package com.timgroup.tucker.info.async;

import com.timgroup.tucker.info.Component;
import com.timgroup.tucker.info.ComponentStateChangeCallback;
import com.timgroup.tucker.info.Report;

public class StatusChangedAdapter implements StatusUpdated {
    private final Component component;
    private final ComponentStateChangeCallback callback;

    private volatile Report previousState = null;


    public StatusChangedAdapter(Component component, ComponentStateChangeCallback callback) {
        this.component = component;
        this.callback = callback;
    }

    @Override
    public void apply(Report currentState) {
        if (previousState != null && currentState.getStatus() != previousState.getStatus()) {
            callback.stateChanged(component, previousState, currentState);
        }
        previousState = currentState;
    }
}
