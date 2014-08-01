package com.timgroup.tucker.info.status;

import java.util.ArrayList;
import java.util.List;

import com.timgroup.tucker.info.Component;
import com.timgroup.tucker.info.component.VersionComponent;

public class AsyncStatusPageGenerator implements ApplicationReportGenerator {

    private final StatusPageGenerator statusPageGenerator;
    private final List<AsyncComponent> components;
   

    public AsyncStatusPageGenerator(String applicationId, VersionComponent versionComponent, List<Component> components) {
        this.statusPageGenerator = new StatusPageGenerator(applicationId, versionComponent);
        this.components = wrapInAsync(components);
    }

    private List<AsyncComponent> wrapInAsync(List<Component> wrapped) {
        List<AsyncComponent> runnableComponents = new ArrayList<AsyncComponent>(wrapped.size());
        for (Component component: wrapped) {
            AsyncComponent async = AsyncComponent.wrapping(component).build();
            runnableComponents.add(async);
            statusPageGenerator.addComponent(async);
        }
        return runnableComponents;
    }

    @Override
    public Component getVersionComponent() {
        return statusPageGenerator.getVersionComponent();
    }

    @Override
    public StatusPage getApplicationReport() {
        return statusPageGenerator.getApplicationReport();
    }
    
    public void start() {
        for (AsyncComponent asyncComponent: components) {
            asyncComponent.start();
        }
    }
    
    public void stop() throws InterruptedException {
        // TODO: exception handling
        for (AsyncComponent asyncComponent: components) {
            asyncComponent.stop();
        }
    }

}
