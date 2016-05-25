package com.timgroup.tucker.info;

import java.util.function.Supplier;

public abstract class Component {
    private final String id;
    private final String label;
    
    public Component(String id, String label) {
        this.id = id;
        this.label = label;
    }
    
    public final String getId() {
        return id;
    }
    
    public final String getLabel() {
        return label;
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
