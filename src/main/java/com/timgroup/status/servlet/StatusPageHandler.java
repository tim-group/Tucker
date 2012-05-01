package com.timgroup.status.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import com.timgroup.status.ApplicationReport;
import com.timgroup.status.StatusPage;

public class StatusPageHandler {

    private static final String UTF_8 = "UTF-8";

    private final Map<String, Handler> dispatch = new HashMap<String, Handler>();

    public StatusPageHandler() {
        dispatch.put(null, new RedirectTo("/status"));
        dispatch.put("", new RedirectTo("/status"));
        dispatch.put("/health", new TextWriter("healthy")); // or "unwell"
        dispatch.put("/stoppable", new TextWriter("safe")); // or "ill"
        dispatch.put("/version", new TextWriter("0.0.0"));
        dispatch.put("/status", new StatusPageWriter(null));
        dispatch.put("/status-page.dtd", new ResourceWriter(StatusPage.DTD_FILENAME, "application/xml-dtd"));
        dispatch.put("/status-page.css", new ResourceWriter(StatusPage.CSS_FILENAME, "text/css"));
    }

    public void handle(String path, WebResponse response) throws IOException {
        if (dispatch.containsKey(path)) {
            dispatch.get(path).handle(response);
        } else {
            response.reject(HttpServletResponse.SC_NOT_FOUND, "try asking for .../status/");
        }
    }

    public void setStatusPage(StatusPage statusPage) {
        dispatch.put("/status", new StatusPageWriter(statusPage));
    }

    private interface Handler {
        void handle(WebResponse response) throws IOException;
    }

    private static final class RedirectTo implements Handler {
        private final String targetPath;

        public RedirectTo(String targetPath) {
            this.targetPath = targetPath;
        }
        
        @Override public void handle(WebResponse response) throws IOException {
            response.redirect(targetPath);
        }
    }

    private static final class StatusPageWriter implements Handler {
        private final StatusPage statusPage;

        public StatusPageWriter(StatusPage statusPage) {
            this.statusPage = statusPage;
        }
        
        @Override public void handle(WebResponse response) throws IOException {
            OutputStream out = response.respond("text/xml", UTF_8);
            ApplicationReport report = statusPage.getApplicationReport();
            report.render(new OutputStreamWriter(out, UTF_8));
        }
    }
    
    private static final class TextWriter implements Handler {
        private String text;

        public TextWriter(String text) {
            this.text = text;
        }
        
        @Override public void handle(WebResponse response) throws IOException {
            OutputStream out = response.respond("text/plain", UTF_8);
            out.write(text.getBytes(Charset.forName(UTF_8)));
            out.close();
        }
    }
    
    private static final class ResourceWriter implements Handler {
        private final String resourceName;
        private final String contentType;

        public ResourceWriter(String resourceName, String contentType) {
            this.resourceName = resourceName;
            this.contentType = contentType;
        }
        
        @Override public void handle(WebResponse response) throws IOException {
            InputStream resource = StatusPage.class.getResourceAsStream(resourceName);
            if (resource == null) {
                response.reject(HttpServletResponse.SC_NOT_FOUND, "could not find resource with name " + resourceName);
                return;
            }
            OutputStream output = response.respond(contentType, UTF_8);
            IOUtils.copy(resource, output);
        }
    }
}
