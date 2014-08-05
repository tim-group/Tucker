package com.timgroup.tucker.info.status;

import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.timgroup.tucker.info.Component;
import com.timgroup.tucker.info.status.AsyncComponent.Settings;

public class AsyncComponentScheduler {

    private static final long NO_INITIAL_DELAY = 0;
    
    private final List<AsyncComponent> components;
    private final ScheduledExecutorService executor;


    public AsyncComponentScheduler(Settings settings, List<Component> components) {
        List<AsyncComponent> asyncComponents = new ArrayList<AsyncComponent>(components.size());
        for (Component synchronousComponent: components) {
            asyncComponents.add(AsyncComponent.wrapping(synchronousComponent, settings));
        }
        this.components = unmodifiableList(asyncComponents);
        this.executor = Executors.newScheduledThreadPool(components.size());
    }

    public AsyncComponentScheduler(List<AsyncComponent> components) {
        this.components = unmodifiableList(new ArrayList<AsyncComponent>(components));
        this.executor = Executors.newScheduledThreadPool(components.size());
    }

    public void start() {
        for (AsyncComponent asyncComponent: components) {
            executor.scheduleWithFixedDelay(
                    asyncComponent.updateStatusRunnable(), 
                    NO_INITIAL_DELAY, 
                    asyncComponent.repeat, 
                    asyncComponent.repeatTimeUnit);
        }
    }
    
    public void addComponentsTo(StatusPageGenerator generator) {
        for (AsyncComponent asyncComponent: components) {
            generator.addComponent(asyncComponent);
        }
    }
    
    public void stop() throws InterruptedException {
      executor.shutdown();
      executor.awaitTermination(1, TimeUnit.SECONDS);
    }
}
