package com.timgroup.tucker.info;

public final class Report {
    
    private static final Object NO_VALUE = new Object();
    
    public static Status worstStatus(Iterable<Report> reports) {
        Status worst = Status.OK;
        for (Report report : reports) {
            worst = worst.or(report.getStatus());
        }
        return worst;
    }
    
    private final Status status;
    private final Object value;
    
    public Report(Status status, Object value) {
        this.status = status;
        this.value = (value == null) ? NO_VALUE : value;
    }
    
    public Report(Status status) {
        this(status, NO_VALUE);
    }
    
    public Report(Throwable e) {
        this(Status.CRITICAL, e);
    }
    
    public Status getStatus() {
        return status;
    }
    
    public boolean hasValue() {
        return value != NO_VALUE;
    }
    
    public boolean isSuccessful() {
        return !(value instanceof Throwable);
    }
    
    public Object getValue() {
        return value;
    }
    
    public Throwable getException() {
        return (Throwable) value;
    }
    
}
