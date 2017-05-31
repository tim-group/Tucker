package com.timgroup.tucker.info;

import java.util.Optional;
import java.util.function.UnaryOperator;

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
    private final Optional<Runbook> runbook;

    public Report(Status status, Object value, Optional<Runbook> runbook) {
        this.status = status;
        this.value = (value == null) ? NO_VALUE : value;
        this.runbook = runbook;
    }

    public Report(Status status, Object value, Runbook runbook) {
        this(status, value, Optional.of(runbook));
    }

    public Report(Status status, Object value) {
        this(status, value, Optional.empty());
    }

    public Report(Status status) {
        this(status, NO_VALUE);
    }
    
    public Report(Throwable e) {
        this(Status.CRITICAL, e, Optional.empty());
    }

    public Report(Throwable e, Optional<Runbook> optionalRunbook) {
        this(Status.CRITICAL, e, optionalRunbook);
    }

    public Status getStatus() {
        return status;
    }

    public Report withRunbook(Runbook runbook) {
        return new Report(status, value, runbook);
    }

    public Report mapValue(UnaryOperator<Object> operator) {
        return new Report(status, operator.apply(value), runbook);
    }

    public Report mapStatus(UnaryOperator<Status> operator) {
        return new Report(operator.apply(status), value, runbook);
    }

    public Report withStatusNoWorseThan(Status notWorse) {
        if (status.compareTo(notWorse) < 0) {
            return new Report(notWorse, value, runbook);
        }
        return this;
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

    public Optional<Runbook> getRunbook() {
        return runbook;
    }

    public boolean hasRunbook() {
        return runbook.isPresent();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Report other = (Report) obj;
        if (status != other.status)
            return false;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Report [status=" + status + ", value=" + value + "]";
    }

}
