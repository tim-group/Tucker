package com.timgroup.tucker.info.component;

import com.timgroup.tucker.info.Report;
import com.timgroup.tucker.info.Status;

/**
 * Reports a version based on the metadata for a specified anchor class.
 * <p>
 * The version ultimately comes from an Implementation-Version entry in the manifest of the jar file containing the class.
 *
 * @see <a href="http://docs.oracle.com/javase/6/docs/technotes/guides/jar/jar.html">JavaSE reference</a>
 */
public final class JarVersionComponent extends VersionComponent {

    private final Class<?> anchorClass;

    public JarVersionComponent(Class<?> anchorClass) {
        this.anchorClass = anchorClass;
    }

    @Override
    public Report getReport() {
        return new Report(Status.INFO, anchorClass.getPackage().getImplementationVersion());
    }

}
