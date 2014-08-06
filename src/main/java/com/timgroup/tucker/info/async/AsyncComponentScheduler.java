package com.timgroup.tucker.info.async;

import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.timgroup.tucker.info.Component;
import com.timgroup.tucker.info.status.StatusPageGenerator;

public class AsyncComponentScheduler {

    private static final long NO_INITIAL_DELAY = 0L;
    
    private final List<AsyncComponent> components;
    private final ScheduledExecutorService executor;

    private AsyncComponentScheduler(List<AsyncComponent> components) {
        this.components = components;
        this.executor = Executors.newScheduledThreadPool(components.size());
    }
    
    public static AsyncComponentScheduler createFromAsync(List<AsyncComponent> components) {
        return new AsyncComponentScheduler(unmodifiableList(new ArrayList<AsyncComponent>(components)));
    }
    
    public static AsyncComponentScheduler createFromSynchronous(AsyncSettings settings, List<Component> synchronousComponents) {
        List<AsyncComponent> asyncComponents = new ArrayList<AsyncComponent>(synchronousComponents.size());
        for (Component synchronousComponent: synchronousComponents) {
            asyncComponents.add(AsyncComponent.wrapping(synchronousComponent, settings));
        }
        return createFromAsync(asyncComponents);
    }

    public void start() {
        for (final AsyncComponent asyncComponent: components) {
            executor.scheduleWithFixedDelay(
                    updateComponentRunnable(asyncComponent), 
                    NO_INITIAL_DELAY, 
                    asyncComponent.getRepeat(), 
                    asyncComponent.getRepeatTimeUnit());
        }
    }

    private Runnable updateComponentRunnable(final AsyncComponent asyncComponent) {
        return new Runnable() {
            @Override public void run() {
                asyncComponent.update(); 
            } 
        };
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
