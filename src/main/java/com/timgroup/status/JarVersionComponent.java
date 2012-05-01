package com.timgroup.status;

/**
 * Reports a version based on the metadata for a specified anchor class. The
 * version ultimately comes from an Implementation-Version entry in the manifest
 * of the jar file containing the class.
 * 
 * <p>
 * See {@link http://docs.oracle.com/javase/6/docs/technotes/guides/jar/jar.html}
 */
public class JarVersionComponent extends Component {
    
    private final Class<?> anchorClass;
    
    public JarVersionComponent(Class<?> anchorClass) {
        super("version", "Version");
        this.anchorClass = anchorClass;
    }
    
    @Override
    public Report getReport() {
        return new Report(Status.INFO, anchorClass.getPackage().getImplementationVersion());
    }
    
}
