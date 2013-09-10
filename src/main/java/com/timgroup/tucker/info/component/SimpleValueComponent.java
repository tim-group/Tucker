package com.timgroup.tucker.info.component;

import com.timgroup.tucker.info.Component;
import com.timgroup.tucker.info.Report;
import com.timgroup.tucker.info.Status;

public final class SimpleValueComponent extends Component {

    private volatile Report state = new Report(Status.INFO, "");

    public SimpleValueComponent(String id, String label) {
        super(id, label);
    }

    public void updateValue(Status status, Object value) {
        state = new Report(status, value);
    }

    @Override
    public Report getReport() {
        return state;
    }

}
