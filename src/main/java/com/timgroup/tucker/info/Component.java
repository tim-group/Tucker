package com.timgroup.tucker.info;

import java.util.Optional;
import java.util.function.Supplier;

public abstract class Component {
    private final String id;
    private final String label;
    private final Optional<Runbook> runbook;
    
    public Component(String id, String label) {
        this(id, label, Optional.empty());
    }

    public Component(String id, String label, Runbook uncaughtExceptionRunbook) {
        this(id, label, Optional.of(uncaughtExceptionRunbook));
    }

    public Component(String id, String label, Optional<Runbook> uncaughtExceptionRunbook) {
        this.id = id;
        this.label = label;
        this.runbook = uncaughtExceptionRunbook;
    }
    
    public final String getId() {
        return id;
    }
    
    public final String getLabel() {
        return label;
    }
    
    public Optional<Runbook> getRunbook() {
        return runbook;
    }

    public boolean hasRunbook() {
        return runbook.isPresent();
    }

    public abstract Report getReport();

    public static Component supplyReport(String id, String label, Supplier<Report> supplier) {
        return new Component(id, label) {
            @Override
            public Report getReport() {
                return supplier.get();
            }
        };
    }

    public static Component supplyInfo(String id, String label, Supplier<String> supplier) {
        return new Component(id, label) {
            @Override
            public Report getReport() {
                return new Report(Status.INFO, supplier.get());
            }
        };
    }

}
