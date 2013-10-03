package com.timgroup.tucker.info.component;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import com.timgroup.tucker.info.Report;
import com.timgroup.tucker.info.Status;

public final class ServletVersionComponent extends VersionComponent {
    private static final String UNINITIALIZED = "Uninitialized version";

    private final ServletContext context;
    private String version = UNINITIALIZED;

    public ServletVersionComponent(ServletConfig servlet) {
        this.context = servlet.getServletContext();
    }

    @Override
    public Report getReport() {
        return new Report(Status.INFO, getVersion());
    }

    private String getVersion() {
        if (version == UNINITIALIZED) {
            version = loadVersion(context);
        }
        return version;
    }

    public static String loadVersion(ServletContext servletContext) {
        try {
            InputStream manifestStream = servletContext.getResourceAsStream("/META-INF/MANIFEST.MF");
            if (manifestStream == null) {
                return null;
            }
            try {
                Manifest manifest = new Manifest(manifestStream);
                Attributes attributes = manifest.getMainAttributes();
                return attributes.getValue("Implementation-Version");
            } finally {
                closeSilently(manifestStream);
            }
        } catch (Exception e) {
            return null;
        }
    }

    private static void closeSilently(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException e) {
            // Ignore
        }
    }
}
