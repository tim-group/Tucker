package com.timgroup.status;

public class Version extends Component {
    
    private final Class<?> anchorClass;
    
    public Version(String id, String label, Class<?> anchorClass) {
        super(id, label);
        this.anchorClass = anchorClass;
    }
    
    public Version(Class<?> anchorClass) {
        this("version", "Version", anchorClass);
    }
    
    @Override
    public Report getReport() {
        return new Report(Status.INFO, anchorClass.getPackage().getImplementationVersion());
    }
    
}
