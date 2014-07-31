package com.timgroup.tucker.info.status;

import static java.util.Calendar.JULY;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

import com.timgroup.tucker.info.Component;
import com.timgroup.tucker.info.Report;
import com.timgroup.tucker.info.Status;
import com.timgroup.tucker.info.status.AsyncComponent.Clock;
import com.timgroup.tucker.info.status.AsyncComponent.Consumer;

public class AsyncComponentTest {

    private final Component hardcodedComponent = new Component("my-test-component-id", "My Test Component Label") {
        @Override
        public Report getReport() {
            return new Report(Status.OK, "It's all good.");
        }
    };

    private Component neverReturnsComponent(final Semaphore invoked) {
        return new Component("my-never-returning-component-id", "My Never Returning Component") {
            @Override
            public Report getReport() {
                try {
                    invoked.release();
                    new CountDownLatch(1).await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                throw new IllegalStateException("Should never have completed");
            }
        };
    }

    private Component nthCallNeverReturns(final int callsThatWillReturnQuickly) {
        return new Component("my-eventually-never-returns-component-id", "My Eventually Never Returns Component") {
            private final Semaphore quickReturnSemaphore = new Semaphore(callsThatWillReturnQuickly);

            @Override
            public Report getReport() {
                try {
                    quickReturnSemaphore.acquire();
                    return new Report(Status.OK, "Everything's fine");
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    @Test
    public void returnsReportForComponentThatIsStillPending() {
        AsyncComponent asyncComponent = AsyncComponent.wrapping(hardcodedComponent);

        Report report = asyncComponent.getReport();

        assertEquals(report.getStatus(), Status.INFO);
        assertEquals(report.getValue(), "Pending");
    }

    @Test
    public void returnsWarningStatusWhenReportHasNeverBeenReturnedWithinTimeThreshold() throws InterruptedException {
        Date initialisation = minutesAfterInitialisation(0);
        Date sixMinutesLater = minutesAfterInitialisation(6);
        
        Clock clock = mock(Clock.class);
        when(clock.now()).thenReturn(initialisation, sixMinutesLater);

        Semaphore invoked = new Semaphore(0);
        AsyncComponent asyncComponent = AsyncComponent.wrapping(neverReturnsComponent(invoked), clock, 1, NANOSECONDS, Consumer.NOOP);
        asyncComponent.start();

        invoked.acquire();
        Report report = asyncComponent.getReport();

        assertEquals(Status.WARNING, report.getStatus());
        assertThat(report.getValue().toString(),
                CoreMatchers.containsString("Last run at 2014-07-12T01:00:00 (over 5 minutes ago): Pending"));
    }

    @Test
    public void returnsWarningStatusForStaleReport() throws Exception {
        Date initialised = minutesAfterInitialisation(0);
        Date oneMinuteLater = minutesAfterInitialisation(1);
        Date threeMinutesLater = minutesAfterInitialisation(3);
        Date tenMinutesLater = minutesAfterInitialisation(10);

        Clock clock = mock(Clock.class);
        when(clock.now()).thenReturn(initialised, oneMinuteLater, oneMinuteLater, threeMinutesLater, threeMinutesLater, tenMinutesLater);

        final Semaphore componentUpdated = new Semaphore(0);
        final Semaphore reportAsserted = new Semaphore(0);
        Consumer statusUpdated = new Consumer() {
            @Override public void apply(Report report) {
                try {
                    componentUpdated.release();
                    reportAsserted.acquire();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            
        };
        AsyncComponent asyncComponent = AsyncComponent.wrapping(nthCallNeverReturns(2), clock, 1, TimeUnit.NANOSECONDS, statusUpdated);
        asyncComponent.start();

        componentUpdated.acquire();
        assertEquals(new Report(Status.OK, "Everything's fine"), asyncComponent.getReport());
        reportAsserted.release();

        componentUpdated.acquire();
        assertEquals(new Report(Status.OK, "Everything's fine"), asyncComponent.getReport());
        reportAsserted.release();

        Report report = asyncComponent.getReport();

        assertEquals(
                new Report(Status.WARNING, "Last run at 2014-07-12T01:03:00 (over 5 minutes ago): Everything's fine"),
                report);

    }

    private Date minutesAfterInitialisation(int minutes) {
        Calendar calender = Calendar.getInstance();
        calender.setTimeZone(TimeZone.getTimeZone("UTC"));
        calender.set(2014, JULY, 12, 1, minutes, 0);
        return calender.getTime();
    }
    
    @Test
    public void goodCase() {
        
    }
    
    @Test
    public void reschedulesUpdateAfterComponentThrowsException() {
        
    }

    @Test
    public void reschedulesUpdateAfterUpdateHookThrowsException() {
        
    }
}
