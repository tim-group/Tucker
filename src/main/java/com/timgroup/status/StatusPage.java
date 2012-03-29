package com.timgroup.status;

import java.io.IOException;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class StatusPage {
    
    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");
    
    private static final String TAG_APPLICATION = "application";
    private static final String TAG_TIMESTAMP = "timestamp";
    private static final String ATTR_CLASS = "class";
    private static final String ATTR_ID = "id";
    
    private final String applicationId;
    
    public StatusPage(String applicationId) {
        this.applicationId = applicationId;
    }
    
    public void render(StringWriter writer) throws IOException {
        long timestamp = System.currentTimeMillis();
        XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
        try {
            XMLStreamWriter out = xmlOutputFactory.createXMLStreamWriter(writer);
            out.writeStartDocument();
            out.writeDTD(constructDTD(TAG_APPLICATION, "status-page.dtd"));
            
            out.writeStartElement(TAG_APPLICATION);
            out.writeAttribute(ATTR_ID, applicationId);
            out.writeAttribute(ATTR_CLASS, "ok");
            
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
