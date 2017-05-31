package com.timgroup.tucker.info;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public abstract class Component {
    private final String id;
    private final String label;
    private final Optional<Runbook> runbook;
    
    public Component(String id, String label) {
        this(id, label, Optional.empty());
    }

    public Component(String id, String label, Runbook defaultRunbook) {
        this(id, label, Optional.of(defaultRunbook));
    }

    public Component(String id, String label, Optional<Runbook> defaultRunbook) {
        this.id = id;
        this.label = label;
        this.runbook = defaultRunbook;
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

    public Component mapReport(UnaryOperator<Report> operator) {
        return new Component(id, label) {
            @Override
            public Report getReport() {
                return operator.apply(Component.this.getReport());
            }

            @Override
            public String toString() {
                return Component.this.toString();
            }
        };
    }

    public Component mapValue(UnaryOperator<Object> operator) {
        return mapReport(r -> r.mapValue(operator));
    }

    public Component mapStatus(UnaryOperator<Status> operator) { return mapReport(r -> r.mapStatus(operator)); }

    public Component withStatusNoWorseThan(Status notWorse) { return mapReport(r -> r.withStatusNoWorseThan(notWorse)); }

    public static Component supplyReport(String id, String label, Supplier<Report> supplier) {
        return new Component(id, label) {
            @Override
            public Report getReport() {
                return supplier.get();
            }

            @Override
            public String toString() {
                return getId() + ":" + supplier;
            }
        };
    }

    public static Component supplyInfo(String id, String label, Supplier<String> supplier) {
        return new Component(id, label) {
            @Override
            public Report getReport() {
                return new Report(Status.INFO, supplier.get());
            }

            @Override
            public String toString() {
                return getId() + ":info:" + supplier;
            }
        };
    }
}
