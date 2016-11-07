package com.timgroup.tucker.info.component;

import com.timgroup.tucker.info.Component;
import com.timgroup.tucker.info.Report;
import com.timgroup.tucker.info.Runbook;

public class ComponentWithRunbookWrapper extends Component {

    private final Component wrappedComponent;

    public ComponentWithRunbookWrapper(Component wrappedComponent, Runbook runbook) {
        super(wrappedComponent.getId(), wrappedComponent.getLabel(), runbook);
        this.wrappedComponent = wrappedComponent;
    }

    @Override
    public Report getReport() {
        return wrappedComponent.getReport();
    }
}
