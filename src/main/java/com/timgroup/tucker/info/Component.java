package com.timgroup.tucker.info;

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
    
}
