package com.timgroup.tucker.info.httpserver;

import com.sun.net.httpserver.HttpExchange;
import com.timgroup.tucker.info.WebResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public class HttpServerWebResponse implements WebResponse {

    private static final int STATUS_OK = 200;
    private static final int STATUS_MOVED = 302;
    private final HttpExchange exchange;
    private final URI base;

    public HttpServerWebResponse(HttpExchange exchange, URI base) {
        this.exchange = exchange;
        this.base = base;
    }

    @Override
    public void setHeader(String name, String value) throws IOException {
        exchange.getResponseHeaders().add(name, value);
    }

    @Override
    public OutputStream respond(String contentType, String characterEncoding) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", contentType + ";charset=" + characterEncoding);
        exchange.sendResponseHeaders(STATUS_OK, 0);
        return exchange.getResponseBody();
    }

    @Override
    public void respond(int statusCode) throws IOException {
        exchange.sendResponseHeaders(statusCode, -1);
    }

    @Override
    public void reject(int status, String message) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "text/plain;charset=" + StandardCharsets.UTF_8.name());
        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, messageBytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(messageBytes);
        }
    }

    @Override
    public void redirect(String relativePath) throws IOException {
        URI location = base.resolve(relativePath.startsWith("/") ? relativePath.substring(1) : relativePath);
        exchange.getResponseHeaders().add("Location", location.toString());
        exchange.sendResponseHeaders(STATUS_MOVED, -1);
    }

}
