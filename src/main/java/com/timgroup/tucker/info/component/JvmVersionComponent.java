package com.timgroup.tucker.info.component;

import com.timgroup.tucker.info.Component;
import com.timgroup.tucker.info.Report;
import com.timgroup.tucker.info.Status;

/**
 * Reports version information about the JVM in which this application is running.
 * <p>
 * The version comes from a query of the java system properties.
 *
 * @see java.lang.System#getProperties
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
