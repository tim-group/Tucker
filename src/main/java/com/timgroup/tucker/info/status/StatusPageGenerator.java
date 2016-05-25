package com.timgroup.tucker.info.status;

import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import com.timgroup.tucker.info.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.timgroup.tucker.info.Component;
import com.timgroup.tucker.info.Report;
import com.timgroup.tucker.info.component.VersionComponent;

public class StatusPageGenerator {
    
    public static final String DTD_FILENAME = "status-page.dtd";
    public static final String CSS_FILENAME = "status-page.css";

    public static final String COMPONENT_STATUS_FORMAT = "{\"eventType\": \"ComponentStatus\", "
            +" \"event\": {\"id\": \"{}\", \"label\": \"{}\", \"status\": \"{}\", \"value\": \"{}\"}}";

    private static final Logger LOGGER = LoggerFactory.getLogger(StatusPageGenerator.class);
    
    private final String applicationId;
    private final VersionComponent versionComponent;
    private final Clock clock;
    private final List<Component> components = new CopyOnWriteArrayList<>();
    
    public StatusPageGenerator(String applicationId, VersionComponent versionComponent) {
        this(applicationId, versionComponent, Clock.systemUTC());
    }

    public StatusPageGenerator(String applicationId, VersionComponent versionComponent, Clock clock) {
        this.applicationId = applicationId;
        this.versionComponent = versionComponent;
        this.clock = clock;
        components.add(versionComponent);
    }
    
    public void addComponent(Component component) {
        components.add(component);
    }
    
    public StatusPage getApplicationReport() {
        Map<Component, Report> componentReports = new LinkedHashMap<>(components.size());
        for (Component component : components) {
            Report report;
            try {
                report = component.getReport();
            } catch (Throwable e) {
                LOGGER.error("exception getting report from component {}", component.getId(), e);
                report = new Report(e);
            }

            if (Status.CRITICAL.equals(report.getStatus()) || Status.WARNING.equals(report.getStatus())) {
                LOGGER.info(COMPONENT_STATUS_FORMAT, component.getId(), component.getLabel(), report.getStatus(), report.getValue());
            }

            componentReports.put(component, report);
        }
        return new StatusPage(applicationId, componentReports, clock);
    }

    public Component getVersionComponent() {
        return versionComponent;
    }
}
