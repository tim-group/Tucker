package com.timgroup.tucker.info.httpserver;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.Charset;

import com.sun.net.httpserver.HttpExchange;
import com.timgroup.tucker.info.WebResponse;

public class HttpServerWebResponse implements WebResponse {

    private static final int STATUS_OK = 200;
    private static final int STATUS_MOVED = 302;
    private static final Charset UTF_8 = Charset.forName("UTF-8");

    private final HttpExchange exchange;
    private final URI base;

    public HttpServerWebResponse(HttpExchange exchange, URI base) {
        this.exchange = exchange;
        this.base = base;
    }

    @Override
    public OutputStream respond(String contentType, String characterEncoding) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", contentType + ";charset=" + characterEncoding);
        exchange.sendResponseHeaders(STATUS_OK, 0);
        return exchange.getResponseBody();
    }

    @Override
    public void reject(int status, String message) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "text/plain;charset=" + UTF_8.name());
        byte[] messageBytes = message.getBytes(UTF_8);
        exchange.sendResponseHeaders(status, messageBytes.length);
        OutputStream out = exchange.getResponseBody();
        out.write(messageBytes);
        out.close();
    }

    @Override
    public void redirect(String relativePath) throws IOException {
        URI location = base.resolve(relativePath.startsWith("/") ? relativePath.substring(1) : relativePath);
        exchange.getResponseHeaders().add("Location", location.toString());
        exchange.sendResponseHeaders(STATUS_MOVED, -1);
    }

}
