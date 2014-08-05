package com.timgroup.tucker.info.status;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

final class TestingSemaphore {
    private final Semaphore semaphore = new Semaphore(0);
    
    void waitFor(String somethingToHappen) {
        try {
            if (!semaphore.tryAcquire(5, SECONDS)) {
                throw new AssertionError(new TimeoutException("Timed out waiting for " + somethingToHappen));
            }
        } catch (InterruptedException e) {
            throw new AssertionError(e);
        }
    }
    
    void completed() {
        semaphore.release();
    }
    
    boolean completedAgainIn(long timeout, TimeUnit unit) {
        try {
            semaphore.drainPermits();
            return semaphore.tryAcquire(timeout, unit);
        } catch (InterruptedException e) {
            throw new AssertionError(e);
        }
    }
}