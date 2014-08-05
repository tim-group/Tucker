package com.timgroup.tucker.info.status;

import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AsyncStatusPageGenerator {

    private static final long NO_INITIAL_DELAY = 0;
    
    private final List<AsyncComponent> components;
    private final ScheduledExecutorService executor;

    public AsyncStatusPageGenerator(List<AsyncComponent> components) {
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
    
    public void addAll(StatusPageGenerator generator) {
        for (AsyncComponent asyncComponent: components) {
            generator.addComponent(asyncComponent);
        }
    }
    
    public void stop() throws InterruptedException {
      executor.shutdown();
      executor.awaitTermination(1, TimeUnit.SECONDS);
    }
}
