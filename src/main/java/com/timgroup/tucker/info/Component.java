package com.timgroup.tucker.info;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public abstract class Component {
    private final String id;
    private final String label;
    private final Runbook runbook;
    
    public Component(String id, String label) {
        this(id, label, null);
    }

    public Component(String id, String label, Runbook defaultRunbook) {
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
    
    public final Optional<Runbook> getRunbook() {
        return Optional.ofNullable(runbook);
    }

    public final boolean hasRunbook() {
        return runbook != null;
    }

    public abstract Report getReport();

    public final Component mapReport(UnaryOperator<Report> operator) {
        return new Component(id, label, runbook) {
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

    public final Component withRunbook(Runbook runbook) {
        return new Component(id, label, Objects.requireNonNull(runbook)) {
            @Override
            public Report getReport() {
                return Component.this.getReport();
            }

            @Override
            public String toString() {
                return Component.this.toString();
            }
        };
    }

    public final Component withRunbook(URI runbookUri) {
        return withRunbook(new Runbook(runbookUri.toString()));
    }

    public final Component mapValue(UnaryOperator<Object> operator) {
        return mapReport(r -> r.mapValue(operator));
    }

    public final Component mapStatus(UnaryOperator<Status> operator) { return mapReport(r -> r.mapStatus(operator)); }

    public final Component withStatusNoWorseThan(Status notWorse) { return mapReport(r -> r.withStatusNoWorseThan(notWorse)); }

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
