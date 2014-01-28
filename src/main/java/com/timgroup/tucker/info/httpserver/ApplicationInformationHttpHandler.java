package com.timgroup.tucker.info.httpserver;

import java.io.IOException;
import java.net.URI;

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
        handler.handle(path, new HttpServerWebResponse(exchange, base));
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

}
