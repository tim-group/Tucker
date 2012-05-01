package com.timgroup.tucker.info.servlet;

import java.io.OutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ServletWebResponseTest {
    
    @Test
    public void respondWrapsHttpServletResponseMethodsInTheNaturalWay() throws Exception {
        HttpServletResponse servletResponse = mock(HttpServletResponse.class);
        
        WebResponse response = new ServletWebResponse(null, servletResponse);
        response.respond("text/rtf", "UTF-8");
        
        verify(servletResponse).setCharacterEncoding("UTF-8");
        verify(servletResponse).setContentType("text/rtf");
    }
    
    @Test
    public void respondReturnsAStreamWhichWrapsTheServletStream() throws Exception {
        HttpServletResponse servletResponse = mock(HttpServletResponse.class);
        ServletOutputStream servletOut = mock(ServletOutputStream.class);
        when(servletResponse.getOutputStream()).thenReturn(servletOut);
        
        WebResponse response = new ServletWebResponse(null, servletResponse);
        OutputStream out = response.respond("text/rtf", "UTF-8");
        out.write(0x23);
        out.close();
        
        verify(servletOut).write(0x23);
        verify(servletOut).close();
    }
    
    @Test
    public void rejectCallsSendError() throws Exception {
        HttpServletResponse servletResponse = mock(HttpServletResponse.class);
        
        WebResponse response = new ServletWebResponse(null, servletResponse);
        response.reject(HttpServletResponse.SC_NOT_FOUND, "gone");
        
        verify(servletResponse).sendError(HttpServletResponse.SC_NOT_FOUND, "gone");
    }
    
    @Test
    public void redirectToPathWithoutLeadingSlashSendsRelativeRedirect() throws Exception {
        HttpServletResponse servletResponse = mock(HttpServletResponse.class);
        
        WebResponse response = new ServletWebResponse(null, servletResponse);
        response.redirect("foo");
        
        verify(servletResponse).sendRedirect("foo");
    }
    
    @Test
    public void redirectToPathWithLeadingSlashSendsAbsoluteRedirect() throws Exception {
        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        HttpServletResponse servletResponse = mock(HttpServletResponse.class);
        when(servletRequest.getContextPath()).thenReturn("/context");
        when(servletRequest.getServletPath()).thenReturn("/servlet");
        
        WebResponse response = new ServletWebResponse(servletRequest, servletResponse);
        response.redirect("/");
        
        verify(servletResponse).sendRedirect("/context/servlet/");
    }
    
}
