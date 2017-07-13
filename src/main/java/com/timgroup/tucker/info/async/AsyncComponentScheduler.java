package com.timgroup.tucker.info.async;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.timgroup.tucker.info.Component;
import com.timgroup.tucker.info.status.StatusPageGenerator;

import static java.util.Collections.unmodifiableList;

public class AsyncComponentScheduler {

    private static final long NO_INITIAL_DELAY = 0L;
    
    private final List<AsyncComponent> components;
    private final List<ScheduledExecutorService> executors;

    private AsyncComponentScheduler(List<AsyncComponent> components) {
        this.components = components;
        this.executors = new ArrayList<>(components.size());
    }
    
    public static AsyncComponentScheduler createFromAsync(List<AsyncComponent> components) {
        return new AsyncComponentScheduler(unmodifiableList(new ArrayList<>(components)));
    }
    
    public static AsyncComponentScheduler createFromSynchronous(List<? extends Component> synchronousComponents) {
        return createFromSynchronous(synchronousComponents, AsyncSettings.settings());
    }
    
    public static AsyncComponentScheduler createFromSynchronous(List<? extends Component> synchronousComponents, AsyncSettings settings) {
        List<AsyncComponent> asyncComponents = new ArrayList<>(synchronousComponents.size());
        for (Component synchronousComponent: synchronousComponents) {
            asyncComponents.add(AsyncComponent.wrapping(synchronousComponent, settings));
        }
        return createFromAsync(asyncComponents);
    }

    public void start() {
        for (AsyncComponent asyncComponent : components) {
            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "Tucker-refresh-" + asyncComponent.getId()));
            executor.scheduleWithFixedDelay(
                    asyncComponent::update,
                    NO_INITIAL_DELAY,
                    asyncComponent.getRepeatInterval().toNanos(),
                    TimeUnit.NANOSECONDS);
            executors.add(executor);
        }
    }

    public void addComponentsTo(StatusPageGenerator generator) {
        for (AsyncComponent asyncComponent: components) {
            generator.addComponent(asyncComponent);
        }
    }
    
    public void stop() throws InterruptedException {
        for (ScheduledExecutorService executor : executors) {
            executor.shutdown();
        }
        for (ScheduledExecutorService executor : executors) {
            executor.awaitTermination(1, TimeUnit.SECONDS);
        }
    }
}
