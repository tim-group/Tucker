package com.timgroup.tucker.info;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

import static java.util.Objects.requireNonNull;

public final class Report {
    public static Status worstStatus(Iterable<Report> reports) {
        Status worst = Status.OK;
        for (Report report : reports) {
            worst = worst.or(report.getStatus());
        }
        return worst;
    }

    private final Status status;
    private final Object value;
    private final Runbook runbook;

    public Report(Status status, Object value, Runbook runbook) {
        this.status = requireNonNull(status);
        this.value = value;
        this.runbook = runbook;
    }

    public Report(Status status, Object value) {
        this(status, value, null);
    }

    public Report(Status status) {
        this(status, null, null);
    }
    
    public Report(Throwable e) {
        this(Status.CRITICAL, e, null);
    }

    public Report(Throwable e, Runbook runbook) {
        this(Status.CRITICAL, e, runbook);
    }

    public Status getStatus() {
        return status;
    }

    public Report withRunbook(Runbook runbook) {
        return new Report(status, value, requireNonNull(runbook));
    }

    public Report withoutRunbook() {
        return new Report(status, value, null);
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

    public static Report combine(BinaryOperator<Status> statusOperator, BinaryOperator<Object> valueOperator, Report... reports) {
        if (reports.length == 0) throw new IllegalArgumentException("reports must not be empty");
        return Arrays.stream(reports).reduce((r1, r2) -> new Report(statusOperator.apply(r1.status, r2.status), valueOperator.apply(r1.value, r2.value), r1.runbook != null ? r1.runbook : r2.runbook)).get();
    }

    public static Report combine(BinaryOperator<Object> operator, Report... reports) {
        return combine(Status::or, operator, reports);
    }

    public static Report combineStringValues(BinaryOperator<String> operator, Report... reports) {
        return combine((v1, v2) -> operator.apply(String.valueOf(v1), String.valueOf(v2)), reports);
    }

    public static Report joinStringValues(String separator, Report... reports) {
        return combineStringValues((s1, s2) -> String.join(separator, s1, s2), reports);
    }

    public static Report joinStringValues(Report... reports) {
        return joinStringValues("\n", reports);
    }

    public boolean hasValue() {
        return value != null;
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
        return Optional.ofNullable(runbook);
    }

    public boolean hasRunbook() {
        return runbook != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Report report = (Report) o;
        return status == report.status &&
                Objects.equals(value, report.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, value);
    }

    @Override
    public String toString() {
        return "Report [status=" + status + ", value=" + value + "]";
    }

}
