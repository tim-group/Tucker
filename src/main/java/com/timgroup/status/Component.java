package com.timgroup.status;

public abstract class Component {
    
    private final String id;
    private final String label;
    
    public Component(String id, String label) {
        this.id = id;
        this.label = label;
    }
    
    public String getId() {
        return id;
    }
    
    public String getLabel() {
        return label;
    }
    
    public abstract Report getReport();
    
}
