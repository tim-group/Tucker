package com.timgroup.tucker.info.component;

import com.timgroup.tucker.info.Component;
import com.timgroup.tucker.info.Report;
import com.timgroup.tucker.info.Status;

/**
 * Reports version information about the JVM in which this application is running. The
 * version comes from a query of the java system properties.
 *
 * <p>
 * See {@link http://docs.oracle.com/javase/6/docs/api/java/lang/System.html#getProperties%28%29}
 */
public class JvmVersionComponent extends Component {

    public JvmVersionComponent() {
        super("jvmversion", "JVM Version");
    }

    @Override
    public Report getReport() {
        return new Report(Status.INFO, System.getProperty("java.version"));
    }

}
