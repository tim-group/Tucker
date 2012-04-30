package com.timgroup.status.servlet;

import java.io.OutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ServletWebResponseTest {
    
    @Test
    public void respondWrapsHttpServletResponseMethodsInTheNaturalWay() throws Exception {
        HttpServletResponse servletResponse = mock(HttpServletResponse.class);
        
        WebResponse response = new ServletWebResponse(servletResponse);
        response.respond("text/rtf", "UTF-8");
        
        verify(servletResponse).setCharacterEncoding("UTF-8");
        verify(servletResponse).setContentType("text/rtf");
    }
    
    @Test
    public void respondReturnsAStreamWhichWrapsTheServletStream() throws Exception {
        HttpServletResponse servletResponse = mock(HttpServletResponse.class);
        ServletOutputStream servletOut = mock(ServletOutputStream.class);
        when(servletResponse.getOutputStream()).thenReturn(servletOut);
        
        WebResponse response = new ServletWebResponse(servletResponse);
        OutputStream out = response.respond("text/rtf", "UTF-8");
        out.write(0x23);
        out.close();
        
        verify(servletOut).write(0x23);
        verify(servletOut).close();
    }
    
}
