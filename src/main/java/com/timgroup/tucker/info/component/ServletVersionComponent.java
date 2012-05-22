package com.timgroup.tucker.info.component;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.servlet.ServletConfig;

import com.timgroup.tucker.info.Report;
import com.timgroup.tucker.info.Status;

public final class ServletVersionComponent extends VersionComponent {
    static private final String UNINITIALIZED = "Uninitialized version";
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
            InputStream manifestStream = null;
            try {
                manifestStream = servlet.getServletContext().getResourceAsStream("/META-INF/MANIFEST.MF");
                Manifest manifest = new Manifest(manifestStream);
                Attributes attributes = manifest.getMainAttributes();
                version = attributes.getValue("Implementation-Version");
            } catch (Exception e) {
                version = null; 
            } finally {
                if (manifestStream != null) {
                    closeSilently(manifestStream);
                }
            }
        }
        return version;
    }

    private static void closeSilently(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException e) {
            // Ignore
        }
    }
}