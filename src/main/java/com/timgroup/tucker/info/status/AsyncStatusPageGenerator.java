package com.timgroup.tucker.info.status;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.timgroup.tucker.info.Component;
import com.timgroup.tucker.info.Report;
import com.timgroup.tucker.info.component.VersionComponent;

public class AsyncStatusPageGenerator implements ApplicationReportGenerator {

    private final StatusPageGenerator statusPageGenerator;
    private final List<AsyncComponent> components;
    private ScheduledExecutorService executor;

    public AsyncStatusPageGenerator(String applicationId, VersionComponent versionComponent, List<Component> components) {
        this.components = wrapInAsync(components);//Collections.unmodifiableList(new ArrayList<Component>(components));
        this.statusPageGenerator = new StatusPageGenerator(applicationId, versionComponent);
        this.executor = Executors.newScheduledThreadPool(components.size());
    }

    private List<AsyncComponent> wrapInAsync(List<Component> wrapped) {
        List<AsyncComponent> runnableComponents = new ArrayList<AsyncComponent>(wrapped.size());
        for (Component component: components) {
            AsyncComponent asyncComponent = new AsyncComponent(component);
            runnableComponents.add(asyncComponent);
            this.executor.scheduleWithFixedDelay(asyncComponent, 0, 60, TimeUnit.SECONDS);
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
        for (Runnable asyncComponent: components) {
            this.executor.scheduleWithFixedDelay(asyncComponent, 0, 60, TimeUnit.SECONDS);
        }
    }
    
    public void stop() throws InterruptedException {
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.SECONDS);
    }
    
    public static class AsyncComponent extends Component implements Runnable {

        private final AtomicReference<Report> current = new AtomicReference<Report>();
        private final Component wrapped;
        
        public AsyncComponent(Component wrapped) {
            super(wrapped.getId(), wrapped.getLabel());
            this.wrapped = wrapped;
        }

        @Override
        public Report getReport() {
            return current.get();
        }
        
        public void update() {
            this.current.set(wrapped.getReport());
        }

        @Override
        public void run() {
            this.update();
        }
    }

}
