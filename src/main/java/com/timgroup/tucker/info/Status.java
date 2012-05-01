package com.timgroup.tucker.info;

public enum Status {
    
    CRITICAL, WARNING, OK, INFO;
    
    public Status or(Status that) {
        return (this.compareTo(that) < 0) ? this : that;
    }
    
}
