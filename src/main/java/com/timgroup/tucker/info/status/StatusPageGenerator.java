package com.timgroup.tucker.info.status;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import com.timgroup.tucker.info.Runbook;
import com.timgroup.tucker.info.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.timgroup.tucker.info.Component;
import com.timgroup.tucker.info.Report;
import com.timgroup.tucker.info.component.VersionComponent;
import com.timgroup.tucker.info.log.JsonFormatter;

public class StatusPageGenerator {

    public static final String DTD_FILENAME = "status-page.dtd";
    public static final String CSS_FILENAME = "status-page.css";

    private static final Logger LOGGER = LoggerFactory.getLogger(StatusPageGenerator.class);

    private final String applicationId;
    private final VersionComponent versionComponent;
    private final Clock clock;
    private final List<Component> components = new CopyOnWriteArrayList<>();

    public StatusPageGenerator(String applicationId, VersionComponent versionComponent) {
        this(applicationId, versionComponent, Clock.systemDefaultZone());
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
                if (!report.hasRunbook() && component.hasRunbook()) {
                    report = report.withRunbook(component.getRunbook().get());
                }
            } catch (Throwable e) {
                LOGGER.error("exception getting report from component {}", component.getId(), e);
                report = new Report(e, component.getRunbook());
            }

            if (Status.CRITICAL.equals(report.getStatus()) || Status.WARNING.equals(report.getStatus())) {
                try (JsonGenerator jgen = JsonFormatter.generate(LOGGER::info)) {
                    jgen.writeStartObject();
                    jgen.writeStringField("eventType", "ComponentStatus");
                    jgen.writeObjectFieldStart("event");
                    jgen.writeStringField("id", component.getId());
                    jgen.writeStringField("label", component.getLabel());
                    jgen.writeStringField("status", String.valueOf(report.getStatus()));
                    jgen.writeStringField("value", String.valueOf(report.getValue()));
                    jgen.writeObjectFieldStart("runbook");
                    jgen.writeStringField("locationUrl", report.getRunbook().map(Runbook::getLocation).orElse(null));
                    jgen.writeEndObject();
                    jgen.writeEndObject();
                    jgen.writeEndObject();
                } catch (IOException e) {
                }
            }

            componentReports.put(component, report);
        }
        return new StatusPage(applicationId, componentReports, Instant.now(clock));
    }

    public Component getVersionComponent() {
        return versionComponent;
    }
}
