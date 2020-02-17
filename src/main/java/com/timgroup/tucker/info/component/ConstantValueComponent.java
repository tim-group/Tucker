package com.timgroup.tucker.info.component;

import com.timgroup.tucker.info.Component;
import com.timgroup.tucker.info.Report;
import com.timgroup.tucker.info.Status;

public final class ConstantValueComponent extends Component {

    private final Report report;

    public ConstantValueComponent(String id, String label, Status status, Object value) {
        this(id, label, new Report(status, value));
    }

    public ConstantValueComponent(String id, String label, Report report) {
        super(id, label);
        this.report = report;
    }

    @Override
    public Report getReport() {
        return report;
    }

}
