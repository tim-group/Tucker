package com.timgroup.status;

/**
 * Reports a version based on the metadata for a specified anchor class. The
 * version ultimately comes from an Implementation-Version entry in the manifest
 * of the jar file containing the class.
 * 
 * <p>
 * See {@link http://docs.oracle.com/javase/6/docs/technotes/guides/jar/jar.html}
 */
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
