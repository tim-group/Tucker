package com.timgroup.tucker.info.status;

import static com.timgroup.tucker.info.Status.OK;
import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;

import org.junit.Test;

import com.timgroup.tucker.info.Component;
import com.timgroup.tucker.info.Report;
import com.timgroup.tucker.info.Status;
import com.timgroup.tucker.info.status.AsyncComponent.Consumer;

public class AsyncStatusPageGeneratorTest {

    @Test
    public void startsAllComponents() throws InterruptedException {
        CountDownLatch scheduledNotification = new CountDownLatch(9);
        AsyncComponent first = quicklyScheduledComponent("first", scheduledNotification);
        AsyncComponent second = quicklyScheduledComponent("second", scheduledNotification);
        AsyncComponent third = quicklyScheduledComponent("third", scheduledNotification);
        
        AsyncStatusPageGenerator generator = new AsyncStatusPageGenerator(asList(first, second, third));
        
        generator.start();
        
        assertTrue("Should schedule repeated updates", scheduledNotification.await(5, MILLISECONDS));
    }

    private AsyncComponent quicklyScheduledComponent(String id, CountDownLatch scheduledNotification) {
        return AsyncComponent.wrapping(
                new SchedulingTestComponent(id, scheduledNotification),
                new AsyncComponent.Settings().withRepeatSchedule(1, MILLISECONDS));
    }
    
    
    @Test
    public void shutsDownThreadPoolAndDoesNotRetrieveComponentStatusAfterBeingStopped() throws InterruptedException {
        final TestingSemaphore componentInvoked = new TestingSemaphore();
        Consumer onUpdate = new Consumer() {
            @Override public void apply(Report report) {
                componentInvoked.completed();
            }
        };

        AsyncComponent asyncComponent = AsyncComponent.wrapping(
                healthyWellBehavedComponent(),
                new AsyncComponent.Settings().withRepeatSchedule(1, NANOSECONDS).withUpdateHook(onUpdate));
        
        AsyncStatusPageGenerator generator = new AsyncStatusPageGenerator(asList(asyncComponent));
        generator.start();
        
        componentInvoked.waitFor("Component to be invoked");
        assertEquals(new Report(OK, "It's all good."), asyncComponent.getReport());
        
        generator.stop();
        assertFalse(componentInvoked.completedAgainIn(100, NANOSECONDS));
    }
    
    private static final class SchedulingTestComponent extends Component {

        private final CountDownLatch scheduledNotification;

        public SchedulingTestComponent(String id, CountDownLatch scheduledNotification) {
            super(id, id);
            this.scheduledNotification = scheduledNotification;
        }

        @Override
        public Report getReport() {
            scheduledNotification.countDown();
            return new Report(Status.OK, "Component " + getId() + " fine");
        }
        
    }
    
    private Component healthyWellBehavedComponent() {
        return new Component("my-test-component-id", "My Test Component Label") {
            @Override public Report getReport() {
                return new Report(OK, "It's all good.");
            }
        };
    }
    
}
