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
public final class JvmVersionComponent extends Component {
    public static final String versionInfoString;

    static {
        String vendorVersion = System.getProperty("java.vendor.version");
        String basicVersion = System.getProperty("java.version");
        if (vendorVersion != null) {
            versionInfoString = basicVersion + " (" + vendorVersion + ")";
        }
        else {
            versionInfoString = basicVersion;
        }
    }

    public JvmVersionComponent() {
        super("jvmversion", "JVM Version");
    }

    @Override
    public Report getReport() {
        return new Report(Status.INFO, versionInfoString);
    }

}
