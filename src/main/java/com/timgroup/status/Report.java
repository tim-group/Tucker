package com.timgroup.status;

public class Report {
    
    private final Status status;
    private final Object value;
    
    public Report(Status status, Object value) {
        this.status = status;
        this.value = value;
    }
    
    public Status getStatus() {
        return status;
    }
    
    public Object getValue() {
        return value;
    }
    
}
