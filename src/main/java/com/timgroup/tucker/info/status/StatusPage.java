package com.timgroup.tucker.info.status;

import java.io.IOException;
import java.io.Writer;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.timgroup.tucker.info.Component;
import com.timgroup.tucker.info.Report;
import com.timgroup.tucker.info.Status;

public class StatusPage {
    
    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");
    private static final XMLOutputFactory XML_OUTPUT_FACTORY = XMLOutputFactory.newInstance();
    private static final JsonFactory JSON_FACTORY = new JsonFactory();

    private static final String TAG_APPLICATION = "application";
    private static final String TAG_COMPONENT = "component";
    private static final String TAG_VALUE = "value";
    private static final String TAG_EXCEPTION = "exception";
    private static final String TAG_TIMESTAMP = "timestamp";
    private static final String ATTR_CLASS = "class";
    private static final String ATTR_ID = "id";
    private static final String ATTR_HOST = "host";
    
    private final String applicationId;
    private final Map<Component, Report> componentReports;
    private final long timestamp;
    private final Status applicationStatus;
    private final String hostname;
    
    public StatusPage(String applicationId, Map<Component, Report> componentReports) {
        this(probeHostname(), applicationId, componentReports);
    }

    public StatusPage(String hostname, String applicationId, Map<Component, Report> componentReports) {
        this.hostname = hostname;
        timestamp = System.currentTimeMillis();
        this.applicationId = applicationId;
        this.componentReports = componentReports;
        applicationStatus = Report.worstStatus(componentReports.values());
    }
    
    public Status getApplicationStatus() {
        return applicationStatus;
    }
    
    public void render(Writer writer) throws IOException {
        try {
            XMLStreamWriter out = XML_OUTPUT_FACTORY.createXMLStreamWriter(writer);
            out.writeStartDocument();
            out.writeDTD(constructDTD(TAG_APPLICATION, StatusPageGenerator.DTD_FILENAME));
            out.writeProcessingInstruction("xml-stylesheet", "type=\"text/css\" href=\"" + StatusPageGenerator.CSS_FILENAME + "\"");
            
            out.writeStartElement(TAG_APPLICATION);
            out.writeAttribute(ATTR_ID, applicationId);
            out.writeAttribute(ATTR_CLASS, applicationStatus.name().toLowerCase());
            out.writeAttribute(ATTR_HOST, hostname);
            
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

    private static String probeHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "localhost";
        }
    }

    public void renderJson(Writer writer) throws IOException {
        JsonGenerator jgen = JSON_FACTORY.createGenerator(writer);
        jgen.writeStartObject();
        jgen.writeStringField(ATTR_ID, applicationId);
        jgen.writeStringField("status", applicationStatus.name().toLowerCase());
        jgen.writeStringField(ATTR_HOST, hostname);
        jgen.writeArrayFieldStart("components");
        for (Map.Entry<Component, Report> componentReport : componentReports.entrySet()) {
            Component component = componentReport.getKey();
            Report report = componentReport.getValue();
            jgen.writeStartObject();
            jgen.writeStringField(ATTR_ID, component.getId());
            jgen.writeStringField("status", report.getStatus().name().toLowerCase());
            jgen.writeStringField("label", component.getLabel());
            if (report.hasValue()) {
                if (report.isSuccessful()) {
                    jgen.writeStringField(TAG_VALUE, String.valueOf(report.getValue()));
                } else {
                    jgen.writeStringField(TAG_EXCEPTION, report.getException().getMessage());
                }
            }
            jgen.writeEndObject();
        }
        jgen.writeEndArray();
        jgen.writeStringField(TAG_TIMESTAMP, formatTime(timestamp));
        jgen.writeEndObject();
        jgen.close();
    }
}
