package com.timgroup.status;

public enum Status {
    
    ERROR, WARN, OK, INFO;
    
    public Status or(Status that) {
        return (this.compareTo(that) < 0) ? this : that;
    }
    
}
