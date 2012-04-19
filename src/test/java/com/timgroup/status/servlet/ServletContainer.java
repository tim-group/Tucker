package com.timgroup.status.servlet;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.util.Arrays;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ServletContainer {
    
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String HEADER_CONTENT_CHARSET = "X-Content-Charset";
    private static final String HEADER_RESPONSE_CODE = "X-Response-Code";
    
    private final int port;
    private final String[] paths;
    private final HttpServer server;
    
    public ServletContainer(HttpServlet servlet, int port, String... paths) throws IOException {
        this.port = port;
        this.paths = paths;
        server = HttpServer.create(new InetSocketAddress(port), 50);
        ServletAdapter handler = new ServletAdapter(servlet);
        for (String path : paths) {
            server.createContext(path, handler);
        }
    }
    
    public void start() {
        server.start();
        System.out.println("server listening on " + port + " at paths " + Arrays.asList(paths));
    }
    
    private static class ServletAdapter implements HttpHandler {
        
        private final HttpServlet servlet;
        
        private ServletAdapter(HttpServlet servlet) {
            this.servlet = servlet;
        }
        
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            System.out.println("handling request for " + exchange.getRequestURI());
            HttpServletRequest request = asServletRequest(exchange);
            HttpServletResponse response = asServletResponse(exchange);
            try {
                servlet.service(request, response);
            } catch (ServletException e) {
                throw new IOException(e);
            }
            exchange.close();
        }
        
        private HttpServletRequest asServletRequest(HttpExchange exchange) {
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getProtocol()).thenReturn(exchange.getProtocol());
            when(request.getMethod()).thenReturn(exchange.getRequestMethod());
            when(request.getServletPath()).thenReturn(exchange.getRequestURI().getPath());
            return request;
        }
        
        private HttpServletResponse asServletResponse(final HttpExchange exchange) {
            HttpServletResponse response = mock(HttpServletResponse.class);
            doAnswer(new SetHeader(exchange, HEADER_RESPONSE_CODE)).when(response).setStatus(anyInt());
            doAnswer(new SetHeader(exchange, HEADER_CONTENT_CHARSET)).when(response).setCharacterEncoding(anyString());
            doAnswer(new SetHeader(exchange, HEADER_CONTENT_TYPE) {
                
                @Override
                protected String value(String value) {
                    return value + "; charset=" + exchange.getResponseHeaders().getFirst(HEADER_CONTENT_CHARSET);
                }
                
            }).when(response).setContentType(anyString());
            try {
                when(response.getWriter()).thenAnswer(new GetWriter(exchange));
            } catch (IOException e) {
                throw new AssertionError(e); // impossible
            }
            return response;
        }
    }
    
    private static class SetHeader implements Answer<Void> {
        
        private final HttpExchange exchange;
        private final String headerName;
        
        private SetHeader(HttpExchange exchange, String headerName) {
            this.exchange = exchange;
            this.headerName = value(headerName);
        }
        
        @Override
        public Void answer(InvocationOnMock invocation) throws Throwable {
            exchange.getResponseHeaders().set(headerName, value(String.valueOf(invocation.getArguments()[0])));
            return null;
        }
        
        protected String value(String value) {
            return value;
        }
        
    }
    
    private static class GetWriter implements Answer<PrintWriter> {
        
        private final HttpExchange exchange;
        
        private GetWriter(HttpExchange exchange) {
            this.exchange = exchange;
        }
        
        @Override
        public PrintWriter answer(InvocationOnMock invocation) throws Throwable {
            String charset = exchange.getResponseHeaders().getFirst(HEADER_CONTENT_CHARSET);
            int status = parseInt(exchange.getResponseHeaders().getFirst(HEADER_RESPONSE_CODE), HttpServletResponse.SC_OK);
            exchange.sendResponseHeaders(status, 0);
            return new PrintWriter(new OutputStreamWriter(exchange.getResponseBody(), charset));
        }
        
        private int parseInt(String string, int defaultValue) {
            return (string != null) ? Integer.parseInt(string) : defaultValue;
        }
        
    }
    
}
