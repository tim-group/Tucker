package com.timgroup.tucker.info.httpserver;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URI;

import org.junit.Before;
import org.junit.Test;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class HttpServerWebResponseTest {

    private final HttpExchange exchange = mock(HttpExchange.class);
    private final Headers headers = new Headers();
    private final ByteArrayOutputStream body = new ByteArrayOutputStream();

    @Before
    public void setUp() {
        when(exchange.getResponseHeaders()).thenReturn(headers);
        when(exchange.getResponseBody()).thenReturn(body);
    }

    @Test
    public void respond() throws Exception {
        OutputStream out = new HttpServerWebResponse(exchange, null).respond("content/type", "PETSCII");

        verify(exchange).sendResponseHeaders(200, 0);
        assertEquals("content/type;charset=PETSCII", headers.getFirst("content-type"));
        assertEquals(body, out);
    }

    @Test
    public void reject() throws Exception {
        String message = "X00398.1984";

        new HttpServerWebResponse(exchange, null).reject(418, message);

        verify(exchange).sendResponseHeaders(418, message.getBytes().length);
        assertEquals("text/plain;charset=UTF-8", headers.getFirst("content-type"));
        assertArrayEquals(message.getBytes(), body.toByteArray());
    }

    @Test
    public void redirect() throws Exception {
        URI base = new URI("http", "tucker.example.org", "/info/", null);
        String relativePath = "/dot";

        new HttpServerWebResponse(exchange, base).redirect(relativePath);

        verify(exchange).sendResponseHeaders(302, -1);
        assertEquals(base.resolve(relativePath.substring(1)).toString(), headers.getFirst("location"));
    }

    @Test
    public void respondWithNoContent() throws Exception {
        new HttpServerWebResponse(exchange, null).respond(204);

        verify(exchange).sendResponseHeaders(204, -1);
    }

}
