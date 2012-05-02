package com.timgroup.tucker.info.component;

import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.servlet.ServletConfig;

import com.timgroup.tucker.info.Report;
import com.timgroup.tucker.info.Status;

public final class ServletVersionComponent extends VersionComponent {
    private final ServletConfig servlet;

    public ServletVersionComponent(ServletConfig servlet) {
        this.servlet = servlet;
    }
    
    @Override
    public Report getReport() {
        String version = "";
        try {
            Manifest manifest = new Manifest(servlet.getServletContext().getResourceAsStream("/META-INF/MANIFEST.MF"));
            Attributes attributes = manifest.getMainAttributes();
            version = attributes.getValue("Implementation-Version");
        } catch (Exception e) {
            
        }
        
        return new Report(Status.INFO, version);
    }
}