package com.timgroup.status;

public class Report {
    
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
        this.value = value;
    }
    
    public Status getStatus() {
        return status;
    }
    
    public Object getValue() {
        return value;
    }
    
}
