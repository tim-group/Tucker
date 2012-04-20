package com.timgroup.status.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.timgroup.status.StatusPage;

import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StatusPageServletTest {
    
    @Test
    public void askingForStatusGetsXMLFromStatusPage() throws Exception {
        StatusPage statusPage = mock(StatusPage.class);
        HttpServletRequest request = mockRequest("/");
        HttpServletResponse response = mock(HttpServletResponse.class);
        PrintWriter writer = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(writer);
        
        StatusPageServlet statusPageServlet = new StatusPageServlet();
        statusPageServlet.setStatusPage(statusPage);
        statusPageServlet.service(request, response);
        
        verify(response).setCharacterEncoding("UTF-8");
        verify(response).setContentType("text/xml"); // must be simply text/xml so Firefox applies CSS; the container will add a charset
        verify(statusPage).render(writer);
    }
    
    @Test
    public void askingForStatusWithoutATrailingSlashGetsARedirectToStatusWithATrailingSlash() throws Exception {
        HttpServletRequest request = mockRequest(null);
        HttpServletResponse response = mock(HttpServletResponse.class);
        
        new StatusPageServlet().service(request, response);
        
        verify(response).sendRedirect("/Foo/status/");
    }
    
    @Test
    public void askingForDTDGetsDTD() throws Exception {
        HttpServletRequest request = mockRequest("/status-page.dtd");
        HttpServletResponse response = mock(HttpServletResponse.class);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        when(response.getOutputStream()).thenReturn(newServletOutputStream(buffer));
        
        new StatusPageServlet().service(request, response);
        
        verify(response).setCharacterEncoding("UTF-8");
        verify(response).setContentType("application/xml-dtd");
        assertArrayEquals(readResource("status-page.dtd"), buffer.toByteArray());
    }
    
    @Test
    public void askingForCSSGetsCSS() throws Exception {
        HttpServletRequest request = mockRequest("/status-page.css");
        HttpServletResponse response = mock(HttpServletResponse.class);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        when(response.getOutputStream()).thenReturn(newServletOutputStream(buffer));
        
        new StatusPageServlet().service(request, response);
        
        verify(response).setCharacterEncoding("UTF-8");
        verify(response).setContentType("text/css");
        assertArrayEquals(readResource("status-page.css"), buffer.toByteArray());
    }
    
    private byte[] readResource(String filename) throws IOException {
        InputStream input = StatusPage.class.getResourceAsStream(filename);
        byte[] bytes = readFully(input);
        return bytes;
    }
    
    @Test
    public void askingForAnythingElseGets404() throws Exception {
        HttpServletRequest request = mockRequest("/rubbish");
        HttpServletResponse response = mock(HttpServletResponse.class);
        
        new StatusPageServlet().service(request, response);
        verify(response).sendError(eq(HttpServletResponse.SC_NOT_FOUND), anyString());
    }
    
    private ServletOutputStream newServletOutputStream(final OutputStream out) {
        return new ServletOutputStream() {
            @Override
            public void write(int b) throws IOException {
                out.write(b);
            }
        };
    }
    
    private byte[] readFully(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        IOUtils.copy(input, output);
        return output.toByteArray();
    }
    
    private HttpServletRequest mockRequest(String pathInfo) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getProtocol()).thenReturn("http");
        when(request.getMethod()).thenReturn("GET");
        when(request.getContextPath()).thenReturn("/Foo");
        when(request.getServletPath()).thenReturn("/status");
        when(request.getPathInfo()).thenReturn(pathInfo);
        return request;
    }
    
}
