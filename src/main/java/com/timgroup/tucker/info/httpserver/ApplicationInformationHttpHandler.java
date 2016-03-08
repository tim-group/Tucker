package com.timgroup.tucker.info.httpserver;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.timgroup.tucker.info.ApplicationInformationHandler;

public class ApplicationInformationHttpHandler implements HttpHandler {

    private final ApplicationInformationHandler handler;
    private final URI base;

    public ApplicationInformationHttpHandler(ApplicationInformationHandler handler, URI base) {
        this.handler = handler;
        this.base = base;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        URI uri = exchange.getRequestURI();
        String path = extractPath(uri);
        HttpServerWebResponse response = new HttpServerWebResponse(exchange, base);

        String callback = extractParameter(uri, "callback");
        if (callback != null) {
            handler.handleJSONP(path, callback, response);
            return;
        }

        handler.handle(path, response);
    }

    private String extractPath(URI uri) {
        URI absoluteUri = base.resolve(uri); // normalise relative URLs
        URI relativeUri = base.relativize(absoluteUri); // then reduce to a path relative to the base

        String path = relativeUri.getPath();
        return prependSlash(path);
    }

    private String prependSlash(String path) {
        return path != null ? "/" + path : null;
    }

    private String extractParameter(URI uri, String name) {
        String queryString = uri.getRawQuery();
        if (queryString == null) {
            return null;
        }
        String[] parts = queryString.split("&");
        String prefix = name + "=";
        for (String part : parts) {
            String decodedPart;
            try {
                decodedPart = URLDecoder.decode(part, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
            if (decodedPart.startsWith(prefix)) {
                return decodedPart.substring(prefix.length());
            }
        }
        return null;
    }
}
