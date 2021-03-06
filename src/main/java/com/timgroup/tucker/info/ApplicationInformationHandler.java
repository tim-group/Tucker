package com.timgroup.tucker.info;

import com.timgroup.tucker.info.status.StatusPage;
import com.timgroup.tucker.info.status.StatusPageGenerator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static java.net.HttpURLConnection.HTTP_UNAVAILABLE;

public class ApplicationInformationHandler {

    private static final String UTF_8 = "UTF-8";

    private final Map<String, Handler> dispatch = new HashMap<>();
    private final Map<String, Handler> jsonpDispatch = new HashMap<>();

    public ApplicationInformationHandler(StatusPageGenerator statusPage, Stoppable stoppable, Health health) {
        dispatch.put(null, new RedirectTo("/status"));
        dispatch.put("", new RedirectTo("/status"));
        dispatch.put("/health", new HealthHandler(health));
        dispatch.put("/ready", new ReadyHandler(health));
        dispatch.put("/stoppable", new StoppableWriter(stoppable));
        dispatch.put("/version", new ComponentHandler(statusPage.getVersionComponent()));
        dispatch.put("/status", new StatusPageHandler(statusPage, health));
        dispatch.put("/status.json", new StatusPageJsonHandler(statusPage, health));
        dispatch.put("/status-page.dtd", new ResourceHandler(StatusPageGenerator.DTD_FILENAME, "application/xml-dtd"));
        dispatch.put("/status-page.css", new ResourceHandler(StatusPageGenerator.CSS_FILENAME, "text/css"));
        jsonpDispatch.put("/status", new StatusPageJsonHandler(statusPage, health));
        jsonpDispatch.put("/status.json", new StatusPageJsonHandler(statusPage, health));
    }

    public void handle(String path, WebResponse response) throws IOException {
        if (dispatch.containsKey(path)) {
            dispatch.get(path).handle(response);
        } else {
            response.reject(HTTP_NOT_FOUND, "try asking for .../status");
        }
    }

    public void handleJSONP(String path, String callback, WebResponse response) throws IOException {
        if (jsonpDispatch.containsKey(path)) {
            jsonpDispatch.get(path).handle(new JSONPResponse(callback, response));
        } else {
            handle(path, response);
        }
    }

    private interface Handler {
        void handle(WebResponse response) throws IOException;
    }

    private static final class JSONPResponse implements WebResponse {
        private final String callback;
        private final WebResponse underlying;

        public JSONPResponse(String callback, WebResponse underlying) {
            this.callback = callback;
            this.underlying = underlying;
        }

        @Override
        public void setHeader(String name, String value) throws IOException {
            underlying.setHeader(name, value);
        }

        @Override
        public OutputStream respond(String contentType, String characterEncoding) throws IOException {
            if (!contentType.equalsIgnoreCase("application/json")) {
                return underlying.respond(contentType, characterEncoding);
            }

            final OutputStream understream = underlying.respond("application/javascript", characterEncoding);

            understream.write(callback.getBytes(characterEncoding));
            understream.write('(');

            return new OutputStream() {
                @Override
                public void write(int b) throws IOException {
                    understream.write(b);
                }

                @Override
                public void write(byte[] b) throws IOException {
                    understream.write(b);
                }

                @Override
                public void write(byte[] b, int off, int len) throws IOException {
                    understream.write(b, off, len);
                }

                @Override
                public void close() throws IOException {
                    understream.write(')');
                    understream.close();
                }
            };
        }

        @Override
        public void respond(int statusCode) throws IOException {
            underlying.respond(statusCode);
        }

        @Override
        public void reject(int status, String message) throws IOException {
            underlying.reject(status, message);
        }

        @Override
        public void redirect(String relativePath) throws IOException {
            underlying.redirect(relativePath);
        }
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

    private static final class StatusPageHandler implements Handler {
        private final StatusPageGenerator statusPageGenerator;
        private final Health health;

        public StatusPageHandler(StatusPageGenerator statusPage, Health health) {
            this.statusPageGenerator = statusPage;
            this.health = health;
        }

        @Override public void handle(WebResponse response) throws IOException {
            try (OutputStreamWriter writer = new OutputStreamWriter(response.respond("text/xml", UTF_8), UTF_8)) {
                StatusPage report = statusPageGenerator.getApplicationReport();
                report.render(writer, health);
            }
        }
    }

    private static final class StatusPageJsonHandler implements Handler {
        private final StatusPageGenerator statusPageGenerator;
        private final Health health;

        public StatusPageJsonHandler(StatusPageGenerator statusPage, Health health) {
            this.statusPageGenerator = statusPage;
            this.health = health;
        }

        @Override public void handle(WebResponse response) throws IOException {
            try (OutputStreamWriter writer = new OutputStreamWriter(response.respond("application/json", UTF_8), UTF_8)) {
                StatusPage report = statusPageGenerator.getApplicationReport();
                report.renderJson(writer, health.get());
            }
        }
    }

    private static final class HealthHandler implements Handler {
        private Health health;

        public HealthHandler(Health health) {
            this.health = health;
        }

        @Override public void handle(WebResponse response) throws IOException {
            try (OutputStream out = response.respond("text/plain", UTF_8)) {
                out.write(health.get().name().getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    private static final class ReadyHandler implements Handler {
        private Health health;

        public ReadyHandler(Health health) {
            this.health = health;
        }

        @Override public void handle(WebResponse response) throws IOException {
            switch(health.get()) {
                case healthy: {
                    response.respond(HTTP_NO_CONTENT);
                    break;
                }
                case ill: {
                    response.respond(HTTP_UNAVAILABLE);
                    break;
                }
            }
        }
    }

    private static final class StoppableWriter implements Handler {
        private final Stoppable stoppable;

        public StoppableWriter(Stoppable stoppable) {
            this.stoppable = stoppable;
        }

        @Override public void handle(WebResponse response) throws IOException {
            try (OutputStream out = response.respond("text/plain", UTF_8)) {
                out.write(stoppable.get().name().getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    private static final class ComponentHandler implements Handler {
        private final Component component;

        public ComponentHandler(Component component) {
            this.component = component;
        }

        @Override public void handle(WebResponse response) throws IOException {
            try (OutputStream out = response.respond("text/plain", UTF_8)) {
                final Report versionReport = component.getReport();
                final String versionString = versionReport.hasValue() ? versionReport.getValue().toString() : "";
                out.write(versionString.getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    private static final class ResourceHandler implements Handler {
        private final String resourceName;
        private final String contentType;

        public ResourceHandler(String resourceName, String contentType) {
            this.resourceName = resourceName;
            this.contentType = contentType;
        }

        @Override public void handle(WebResponse response) throws IOException {
            URL resourceUri = StatusPageGenerator.class.getResource(resourceName);
            if (resourceUri == null) {
                response.reject(HTTP_NOT_FOUND, "could not find resource with name " + resourceName);
                return;
            }
            try (InputStream resource = resourceUri.openStream();
                    OutputStream out = response.respond(contentType, UTF_8)) {
                copy(resource, out);
            }
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
