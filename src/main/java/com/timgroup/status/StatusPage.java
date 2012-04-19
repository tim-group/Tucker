package com.timgroup.status;

import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatusPage {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(StatusPage.class);
    
    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");
    
    private static final String TAG_APPLICATION = "application";
    private static final String TAG_COMPONENT = "component";
    private static final String TAG_VALUE = "value";
    private static final String TAG_EXCEPTION = "exception";
    private static final String TAG_TIMESTAMP = "timestamp";
    private static final String ATTR_CLASS = "class";
    private static final String ATTR_ID = "id";
    
    private final String applicationId;
    private final List<Component> components;
    
    public StatusPage(String applicationId) {
        this.applicationId = applicationId;
        components = new ArrayList<Component>();
    }
    
    public void addComponent(Component component) {
        components.add(component);
    }
    
    private Map<Component, Report> findComponentReports() {
        Map<Component, Report> componentReports = new LinkedHashMap<Component, Report>(components.size());
        for (Component component : components) {
            Report report;
            try {
                report = component.getReport();
            } catch (Throwable e) {
                LOGGER.error("exception getting report from component {}", component.getId(), e);
                report = new Report(e);
            }
            componentReports.put(component, report);
        }
        return componentReports;
    }
    
    private Status findApplicationStatus(Map<Component, Report> componentReports) {
        return Report.worstStatus(componentReports.values());
    }
    
    public void render(Writer writer) throws IOException {
        long timestamp = System.currentTimeMillis();
        
        Map<Component, Report> componentReports = findComponentReports();
        
        XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
        try {
            XMLStreamWriter out = xmlOutputFactory.createXMLStreamWriter(writer);
            out.writeStartDocument();
            out.writeDTD(constructDTD(TAG_APPLICATION, "status-page.dtd"));
            
            out.writeStartElement(TAG_APPLICATION);
            out.writeAttribute(ATTR_ID, applicationId);
            Status applicationStatus = findApplicationStatus(componentReports);
            out.writeAttribute(ATTR_CLASS, applicationStatus.name().toLowerCase());
            
            for (Entry<Component, Report> componentReport : componentReports.entrySet()) {
                Component component = componentReport.getKey();
                Report report = componentReport.getValue();
                out.writeStartElement(TAG_COMPONENT);
                out.writeAttribute(ATTR_ID, component.getId());
                out.writeAttribute(ATTR_CLASS, report.getStatus().name().toLowerCase());
                out.writeCharacters(component.getLabel());
                if (report.hasValue()) {
                    out.writeCharacters(": ");
                    if (report.isSuccessful()) {
                        out.writeStartElement(TAG_VALUE);
                        out.writeCharacters(String.valueOf(report.getValue()));
                        out.writeEndElement();
                    } else {
                        out.writeStartElement(TAG_EXCEPTION);
                        out.writeCharacters(report.getException().getMessage());
                        out.writeEndElement();
                    }
                }
                out.writeEndElement();
            }
            
            out.writeStartElement(TAG_TIMESTAMP);
            out.writeCharacters(formatTime(timestamp));
            out.writeEndElement();
            
            out.writeEndElement();
            out.writeEndDocument();
            out.close();
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }
    
    private String constructDTD(String rootElement, String systemID) {
        return "<!DOCTYPE " + rootElement + " SYSTEM \"" + systemID + "\">";
    }
    
    private String formatTime(long time) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        df.setTimeZone(UTC);
        return df.format(time);
    }
    
}
