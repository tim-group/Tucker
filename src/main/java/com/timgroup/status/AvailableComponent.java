package com.timgroup.status;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Reports whether the application is accepting requests.
 * 
 */
public class AvailableComponent extends Component {
    private final AtomicBoolean available = new AtomicBoolean();
    
    public AvailableComponent() {
        super("available", "Available");
    }
    
    public void makeAvailable() {
        available.set(true);
    }
    
    public void makeUnavailable() {
        available.set(false);
    }
    
    @Override
    public Report getReport() {
        return new Report(Status.INFO, available);
    }
}
