package com.timgroup.tucker.info.component;

import java.io.InputStream;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import com.timgroup.tucker.info.Report;
import com.timgroup.tucker.info.Status;

public final class ServletVersionComponent extends VersionComponent {
    private static final String UNINITIALIZED = "Uninitialized version";

    private final ServletConfig config;
    private String version = UNINITIALIZED;

    public ServletVersionComponent(ServletConfig config) {
        this.config = config;
    }

    @Override
    public Report getReport() {
        return new Report(Status.INFO, getVersion());
    }

    private String getVersion() {
        if (version == UNINITIALIZED) {
            version = loadVersion(config.getServletContext());
        }
        return version;
    }

    public static String loadVersion(ServletContext servletContext) {
        try {
            URL manifestUri = servletContext.getResource("/META-INF/MANIFEST.MF");
            if (manifestUri == null) {
                return null;
            }
            try (InputStream manifestStream = manifestUri.openStream()) {
                Manifest manifest = new Manifest(manifestStream);
                Attributes attributes = manifest.getMainAttributes();
                return attributes.getValue("Implementation-Version");
            }
        } catch (Exception e) {
            return null;
        }
    }
}
