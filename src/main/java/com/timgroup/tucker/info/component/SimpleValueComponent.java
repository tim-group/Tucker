package com.timgroup.tucker.info.component;

import com.timgroup.tucker.info.Component;
import com.timgroup.tucker.info.Report;
import com.timgroup.tucker.info.Status;

public final class SimpleValueComponent extends Component {

    // hold status and value together for thread safety
    private SimpleValueComponent.StateHolder state = new StateHolder(Status.INFO, ""); 

    public SimpleValueComponent(String id, String label) {
        super(id, label);
    }

    public void updateValue(Status status, Object value) {
        state = new StateHolder(status, value);
    }

    @Override public Report getReport() {
        SimpleValueComponent.StateHolder currentState = state;
        return new Report(currentState.status, currentState.value);
    }

    private static final class StateHolder {
        public final Status status;
        public final Object value;
        private StateHolder(Status status, Object value) {
            this.status = status;
            this.value = value;
        }
    }
}