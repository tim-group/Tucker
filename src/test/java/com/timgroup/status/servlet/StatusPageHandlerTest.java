package com.timgroup.status.servlet;

import java.io.ByteArrayOutputStream;

import org.junit.Before;
import org.junit.Test;

import com.timgroup.status.StatusPage;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StatusPageHandlerTest {

    private final StatusPageHandler handler = new StatusPageHandler();
    
    @Before
    public void setup() {
        handler.setStatusPage(new StatusPage("appId"));
    }
    
    @Test
    public void responds_to_version_request() throws Exception {
        final ByteArrayOutputStream responseContent = new ByteArrayOutputStream();
        
        final WebResponse response = mock(WebResponse.class);
        when(response.respond("text/plain", "UTF-8")).thenReturn(responseContent);
        
        handler.handle("/version", response);
        
        verify(response).respond("text/plain", "UTF-8");
        assertEquals("", responseContent.toString());
    }

}
