package com.timgroup.tucker.info;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import com.timgroup.tucker.info.servlet.WebResponse;
import com.timgroup.tucker.info.status.StatusPage;
import com.timgroup.tucker.info.status.StatusPageGenerator;

import static java.net.HttpURLConnection.HTTP_NOT_FOUND;

public class ApplicationInformationHandler {

    private static final String UTF_8 = "UTF-8";

    private final Map<String, Handler> dispatch = new HashMap<String, Handler>();

    public ApplicationInformationHandler(StatusPageGenerator statusPage) {
        dispatch.put(null, new RedirectTo("/status"));
        dispatch.put("", new RedirectTo("/status"));
        dispatch.put("/health", new TextWriter("healthy")); // or "unwell"
        dispatch.put("/stoppable", new TextWriter("safe")); // or "ill"
        dispatch.put("/version", new ComponentWriter(statusPage.getVersionComponent()));
        dispatch.put("/status", new StatusPageWriter(statusPage));
        dispatch.put("/status-page.dtd", new ResourceWriter(StatusPageGenerator.DTD_FILENAME, "application/xml-dtd"));
        dispatch.put("/status-page.css", new ResourceWriter(StatusPageGenerator.CSS_FILENAME, "text/css"));
    }

    public void handle(String path, WebResponse response) throws IOException {
        if (dispatch.containsKey(path)) {
            dispatch.get(path).handle(response);
        } else {
            response.reject(HTTP_NOT_FOUND, "try asking for .../status");
        }
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
        private final StatusPageGenerator statusPage;

        public StatusPageWriter(StatusPageGenerator statusPage) {
            this.statusPage = statusPage;
        }
        
        @Override public void handle(WebResponse response) throws IOException {
            OutputStream out = response.respond("text/xml", UTF_8);
            StatusPage report = statusPage.getApplicationReport();
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
    
    private static final class ComponentWriter implements Handler {
        private final Component component;

        public ComponentWriter(Component component) {
            this.component = component;
        }
        
        @Override public void handle(WebResponse response) throws IOException {
            final OutputStream out = response.respond("text/plain", UTF_8);
            final Report versionReport = component.getReport();
            final String versionString = versionReport.hasValue() ? versionReport.getValue().toString() : "";
            out.write(versionString.getBytes(Charset.forName(UTF_8)));
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
            InputStream resource = StatusPageGenerator.class.getResourceAsStream(resourceName);
            if (resource == null) {
                response.reject(HTTP_NOT_FOUND, "could not find resource with name " + resourceName);
                return;
            }
            OutputStream out = response.respond(contentType, UTF_8);
            copy(resource, out);
            out.close();
        }

        private void copy(InputStream input, OutputStream output) throws IOException {
            final byte[] buffer = new byte[1024];
            int n = 0;
            while (-1 != (n = input.read(buffer))) {
                output.write(buffer, 0, n);
            }
        }
    }
}
