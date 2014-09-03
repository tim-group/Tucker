package com.timgroup.tucker.info.component.pending;

import com.timgroup.tucker.info.Component;
import com.timgroup.tucker.info.Report;

public interface ComponentStateChangeCallback {
    void stateChanged(Component component, Report previous, Report current);
}