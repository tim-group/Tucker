package com.timgroup.tucker.info;

import java.net.URI;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import static java.util.Objects.requireNonNull;

public abstract class Component {
    private final String id;
    private final String label;

    public Component(String id, String label) {
        this.id = requireNonNull(id);
        this.label = requireNonNull(label);
    }

    public final String getId() {
        return id;
    }
    
    public final String getLabel() {
        return label;
    }

    public abstract Report getReport();

    public Component mapReport(UnaryOperator<Report> operator) {
        requireNonNull(operator);
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

    public Component mapReportHandlingError(BiFunction<? super Report, ? super Throwable, Report> handler) {
        requireNonNull(handler);
        return new Component(id, label) {
            @Override
            public Report getReport() {
                Report report;
                try {
                    report = Component.this.getReport();
                } catch (Throwable t) {
                    return handler.apply(null, t);
                }
                return handler.apply(report, null);
            }

            @Override
            public String toString() {
                return Component.this.toString();
            }
        };
    }

    public final Component withRunbook(Runbook runbook) {
        requireNonNull(runbook);
        return mapReportHandlingError((r, t) -> r != null ? r.hasRunbook() ? r : r.withRunbook(runbook) : new Report(t, runbook));
    }

    public final Component withRunbook(URI runbookUri) {
        return withRunbook(new Runbook(runbookUri.toString()));
    }

    public final Component mapValue(UnaryOperator<Object> operator) {
        requireNonNull(operator);
        return mapReport(r -> r.mapValue(operator));
    }

    public final Component mapStatus(UnaryOperator<Status> operator) {
        requireNonNull(operator);
        return mapReport(r -> r.mapStatus(operator));
    }

    public final Component withStatusNoWorseThan(Status notWorse) {
        requireNonNull(notWorse);
        return mapReport(r -> r.withStatusNoWorseThan(notWorse));
    }

    public static Component of(String id, String label, Report report) {
        requireNonNull(report);
        return new Component(id, label) {
            @Override
            public Report getReport() {
                return report;
            }

            @Override
            public String toString() {
                return getId() + ":" + report;
            }
        };
    }

    public static Component supplyReport(String id, String label, Supplier<Report> supplier) {
        requireNonNull(supplier);
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

    public static Component info(String id, String label, String info) {
        Report report = new Report(Status.INFO, info);
        return new Component(id, label) {
            @Override
            public Report getReport() {
                return report;
            }

            @Override
            public String toString() {
                return getId() + ":info:" + info;
            }
        };
    }

    public static Component supplyInfo(String id, String label, Supplier<String> supplier) {
        requireNonNull(supplier);
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

    public static Component combine(String id, String label, BinaryOperator<Status> statusOperator, BinaryOperator<Object> valueOperator, Component... components) {
        if (components.length == 0) throw new IllegalArgumentException("components must not be empty");
        return supplyReport(id, label, () -> {
            Report[] reports = new Report[components.length];
            for (int i = 0; i < components.length; i++) {
                reports[i] = components[i].getReport();
            }
            return Report.combine(statusOperator, valueOperator, reports);
        });
    }

    public static Component combine(String id, String label, BinaryOperator<Object> operator, Component... components) {
        return combine(id, label, Status::or, operator, components);
    }

    public static Component combineStringValues(String id, String label, BinaryOperator<String> operator, Component... components) {
        return combine(id, label, (v1, v2) -> operator.apply(String.valueOf(v1), String.valueOf(v2)), components);
    }

    public static Component joinStringValues(String id, String label, String separator, Component... components) {
        return combineStringValues(id, label, (s1, s2) -> String.join(separator, s1, s2), components);
    }

    public static Component joinStringValues(String id, String label, Component... components) {
        return joinStringValues(id, label, "\n", components);
    }
}
