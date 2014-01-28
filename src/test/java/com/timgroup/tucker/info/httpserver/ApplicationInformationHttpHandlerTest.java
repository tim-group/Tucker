package com.timgroup.tucker.info.httpserver;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.timgroup.tucker.info.ApplicationInformationHandler;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ApplicationInformationHttpHandlerTest {

    private static final String BASE_PATH = "/info/";
    private static final URI BASE_URI;
    static {
        try {
            BASE_URI = new URI("http", "tucker.example.org", BASE_PATH, null);
        } catch (URISyntaxException e) {
            AssertionError e2 = new AssertionError("inconcievable");
            e2.initCause(e);
            throw e2;
        }
    }

    private final ApplicationInformationHandler handler = mock(ApplicationInformationHandler.class);
    private final HttpExchange exchange = mock(HttpExchange.class);

    /**
     * This is what the Javadoc sort of suggests should happen.
     */
    @Test
    public void extractsPathFromAbsoluteUriAndPassesToHandler() throws Exception {
        when(exchange.getRequestURI()).thenReturn(BASE_URI.resolve("status"));

        HttpHandler httpHandler = new ApplicationInformationHttpHandler(handler, BASE_URI);
        httpHandler.handle(exchange);

        verify(handler).handle(eq("/status"), any(HttpServerWebResponse.class));
    }

    /**
     * This is what actually seems to happen.
     */
    @Test
    public void extractsPathFromRelativeUriAndPassesToHandler() throws Exception {
        when(exchange.getRequestURI()).thenReturn(new URI(null, null, BASE_PATH + "status", null));

        HttpHandler httpHandler = new ApplicationInformationHttpHandler(handler, BASE_URI);
        httpHandler.handle(exchange);

        verify(handler).handle(eq("/status"), any(HttpServerWebResponse.class));
    }

}
