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

    private final ServletConfig servlet;
    private String version = UNINITIALIZED;

    public ServletVersionComponent(ServletConfig servlet) {
        this.servlet = servlet;
    }

    @Override
    public Report getReport() {
        return new Report(Status.INFO, getVersion());
    }

    private String getVersion() {
        if (version == UNINITIALIZED) {
            version = loadVersion(servlet.getServletContext());
        }
        return version;
    }

    public static String loadVersion(ServletContext servletContext) {
        InputStream manifestStream = servletContext.getResourceAsStream("/META-INF/MANIFEST.MF");
        try {
            Manifest manifest = new Manifest(manifestStream);
            Attributes attributes = manifest.getMainAttributes();
            return attributes.getValue("Implementation-Version");
        } catch (Exception e) {
            return null;
        } finally {
            closeSilently(manifestStream);
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
