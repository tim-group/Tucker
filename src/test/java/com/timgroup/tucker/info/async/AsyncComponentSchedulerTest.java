package com.timgroup.tucker.info.async;

import com.timgroup.tucker.info.Component;
import com.timgroup.tucker.info.Report;
import com.timgroup.tucker.info.Status;
import org.junit.After;
import org.junit.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import static com.timgroup.tucker.info.Status.OK;
import static com.timgroup.tucker.info.Status.WARNING;
import static com.timgroup.tucker.info.async.ManualClock.initiallyAt;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AsyncComponentSchedulerTest {
    private AsyncComponentScheduler scheduler;

    private final Component healthyWellBehavedComponent = Component.of("my-test-component-id", "My Test Component Label", new Report(OK, "It's all good."));
    
    private AsyncComponentScheduler schedule(AsyncComponent asyncComponent) {
        if (scheduler != null) {
            throw new IllegalStateException();
        }
        scheduler = AsyncComponentScheduler.createFromAsync(singletonList(asyncComponent));
        scheduler.start();
        return scheduler;
    }

    @After
    public void stopScheduler() {
        if (scheduler != null) {
            try {
                scheduler.stop();
            } catch (Exception ignored) {
            }
        }
    }

    @Test
    public void startsAllComponents() throws InterruptedException {
        CountDownLatch scheduledNotification = new CountDownLatch(9);
        AsyncComponent first = quicklyScheduledComponent("first", scheduledNotification);
        AsyncComponent second = quicklyScheduledComponent("second", scheduledNotification);
        AsyncComponent third = quicklyScheduledComponent("third", scheduledNotification);
        
        scheduler = AsyncComponentScheduler.createFromAsync(asList(first, second, third));
        
        scheduler.start();
        
        assertTrue("Should schedule repeated updates", scheduledNotification.await(1, SECONDS));
    }

    private AsyncComponent quicklyScheduledComponent(String id, CountDownLatch scheduledNotification) {
        return AsyncComponent.wrapping(
                new SchedulingTestComponent(id, scheduledNotification),
                AsyncSettings.settings().withRepeatSchedule(1, MILLISECONDS));
    }
    
    @Test
    public void shutsDownThreadPoolAndDoesNotRetrieveComponentStatusAfterBeingStopped() throws InterruptedException {
        final TestingSemaphore componentInvoked = new TestingSemaphore();
        StatusUpdated onUpdate = report -> componentInvoked.completed();

        AsyncComponent asyncComponent = AsyncComponent.wrapping(
                healthyWellBehavedComponent,
                AsyncSettings.settings().withRepeatSchedule(1, MILLISECONDS).withUpdateHook(onUpdate));
        
        scheduler = schedule(asyncComponent);
        
        componentInvoked.waitFor("Component to be invoked");
        assertEquals(new Report(OK, "It's all good."), asyncComponent.getReport());
        
        scheduler.stop();
        assertFalse(componentInvoked.completedAgainIn(100, MILLISECONDS));
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
    
    @Test
    public void returnsWarningStatusWhenReportHasNeverBeenReturnedWithinTimeThreshold() {
        ManualClock clock = initiallyAt(Instant.parse("2014-07-12T01:00:00Z"));

        TestingSemaphore invoked = new TestingSemaphore();
        AsyncComponent asyncComponent = AsyncComponent.wrapping(neverReturnsComponent(invoked),
                AsyncSettings.settings()
                .withClock(clock)
                .withRepeatSchedule(1, MILLISECONDS)
                .withStalenessLimit(4, MINUTES));
        
        schedule(asyncComponent);

        clock.bump(Duration.ofMinutes(6));

        invoked.waitFor("Component to be invoked");
        Report report = asyncComponent.getReport();

        assertEquals(WARNING, report.getStatus());
        assertThat(
            report.getValue().toString(),
            containsString("Last run at 2014-07-12T01:00:00Z (over PT4M ago): Not yet run"));
    }

    @Test
    public void canStopEvenIfHaventStarted() throws InterruptedException {
        AsyncComponent asyncComponent = AsyncComponent.wrapping(
                healthyWellBehavedComponent,
                AsyncSettings.settings().withRepeatSchedule(1, MILLISECONDS));

        scheduler = AsyncComponentScheduler.createFromAsync(singletonList(asyncComponent));

        scheduler.stop();
    }

    private Component neverReturnsComponent(final TestingSemaphore invoked) {
        return Component.supplyReport("my-never-returning-component-id", "My Never Returning Component", () -> {
            try {
                invoked.completed();
                new CountDownLatch(1).await();
            } catch (InterruptedException e) {
                throw new AssertionError(e);
            }
            throw new IllegalStateException("Should never have completed");
        });
    }

    @Test
    public void returnsWarningStatusForStaleReport() throws Exception {
        ManualClock clock = initiallyAt(Instant.parse("2014-07-12T01:00:00Z"));

        final TestingSemaphore componentUpdated = new TestingSemaphore();
        final TestingSemaphore reportAsserted = new TestingSemaphore();
        StatusUpdated statusUpdated = report -> {
            componentUpdated.completed();
            reportAsserted.waitFor("assertion to be checked");
        };
        AsyncComponent asyncComponent = AsyncComponent.wrapping(nthCallNeverReturns(2),
                AsyncSettings.settings()
                .withClock(clock)
                .withRepeatSchedule(1, MILLISECONDS)
                .withUpdateHook(statusUpdated));
        
        schedule(asyncComponent);

        clock.bump(Duration.ofMinutes(1));

        componentUpdated.waitFor("Component to be invoked");
        assertEquals(new Report(OK, "Everything's fine"), asyncComponent.getReport());
        reportAsserted.completed();

        clock.bump(Duration.ofMinutes(2));

        componentUpdated.waitFor("Component to be invoked");
        assertEquals(new Report(OK, "Everything's fine"), asyncComponent.getReport());
        reportAsserted.completed();

        clock.bump(Duration.ofMinutes(7));

        Report report = asyncComponent.getReport();

        assertEquals(
                new Report(WARNING, "Last run at 2014-07-12T01:03:00Z (over PT5M ago): Everything's fine"),
                report);

    }
    
    private Component nthCallNeverReturns(final int callsThatWillReturnQuickly) {
        Semaphore quickReturnSemaphore = new Semaphore(callsThatWillReturnQuickly);
        return Component.supplyReport("my-eventually-never-returns-component-id", "My Eventually Never Returns Component", () -> {
            try {
                quickReturnSemaphore.acquire();
                return new Report(OK, "Everything's fine");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    public void reschedulesUpdateAfterComponentThrowsException() {
        final TestingSemaphore componentInvoked = new TestingSemaphore();
        final TestingSemaphore assertionSemaphore = new TestingSemaphore();
        StatusUpdated onUpdate = report -> {
            componentInvoked.completed();
            assertionSemaphore.waitFor("assertion to be checked");
        };

        AsyncComponent asyncComponent = AsyncComponent.wrapping(initiallyThrowsExceptionComponent(),
                AsyncSettings.settings().withRepeatSchedule(1, MILLISECONDS).withUpdateHook(onUpdate));
        
        schedule(asyncComponent);
        
        componentInvoked.waitFor("Component to be invoked");
        Report report = asyncComponent.getReport();
        
        assertEquals(WARNING, report.getStatus());
        assertThat(report.getException(), is(instanceOf(IllegalStateException.class)));
        assertionSemaphore.completed();
        
        componentInvoked.waitFor("Component to be invoked");
        assertEquals(new Report(OK, "Recovered"), asyncComponent.getReport());
    }
    
    private Component initiallyThrowsExceptionComponent() {
        AtomicInteger timesCalled = new AtomicInteger(0);
        return Component.supplyReport("my-initially-throws-exception-id", "My Initially Throws Exception Component", () -> {
            if (timesCalled.getAndIncrement() == 0) {
                throw new IllegalStateException("Thrown by component");
            }
            return new Report(OK, "Recovered");
        });
    }

    @Test
    public void reschedulesUpdateAfterUpdateHookThrowsException() {
        final TestingSemaphore componentInvoked = new TestingSemaphore();
        StatusUpdated onUpdate = new StatusUpdated() {
            private final AtomicInteger timesCalled = new AtomicInteger(0);
            @Override public void accept(Report report) {
                componentInvoked.completed();
                if (timesCalled.getAndIncrement() == 0) {
                    throw new IllegalArgumentException("Thrown by update hook");
                }
            }
        };

        AsyncComponent asyncComponent = AsyncComponent.wrapping(
                healthyWellBehavedComponent,
                AsyncSettings.settings().withRepeatSchedule(1, MILLISECONDS).withUpdateHook(onUpdate));
        
        schedule(asyncComponent);
        
        componentInvoked.waitFor("Component to be invoked");
        assertEquals(new Report(OK, "It's all good."), asyncComponent.getReport());
        
        componentInvoked.waitFor("Component to be invoked");
        assertEquals(new Report(OK, "It's all good."), asyncComponent.getReport());
    }

    @Test
    public void reschedulesWhenAnErrorIsThrownDuringUpdate() {
        final TestingSemaphore componentInvoked = new TestingSemaphore();

        AsyncComponent asyncComponent = AsyncComponent.wrapping(
                throwsErrorComponent(componentInvoked),
                AsyncSettings.settings().withRepeatSchedule(1, MILLISECONDS));

        schedule(asyncComponent);

        componentInvoked.waitFor("Component to be invoked");

        assertTrue(componentInvoked.completedAgainIn(50, MILLISECONDS));
    }
    
    private Component throwsErrorComponent(final TestingSemaphore componentInvoked) {
        return Component.supplyReport("my-error-throwing-component-id", "My Error-Throwing Component", () -> {
            componentInvoked.completed();
            throw new NoSuchMethodError("Unrecoverable error from component");
        });
    }
}
