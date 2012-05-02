package com.timgroup.tucker.info.component;

import java.io.ByteArrayInputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.junit.Test;

import com.timgroup.tucker.info.Status;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ServletVersionComponentTest {

    @Test
    public void defaultIdAndLabelAreSuitable() throws Exception {
        final ServletConfig servlet = mock(ServletConfig.class);
        
        assertEquals("version", new ServletVersionComponent(servlet).getId());
        assertEquals("Version", new ServletVersionComponent(servlet).getLabel());
    }
    
    @Test
    public void reportStatusIsInfo() throws Exception {
        final ServletConfig servlet = mock(ServletConfig.class);
        
        assertEquals(Status.INFO, new ServletVersionComponent(servlet).getReport().getStatus());
    }
    
    @Test
    public void reportValueIsImplementationVersionOfPackageContainingAnchorClass() throws Exception {
        final ServletConfig servlet = mock(ServletConfig.class);
        final ServletContext context = mock(ServletContext.class);
        final String versionEntry = "Implementation-Version: 1.2.3\n";
        final ByteArrayInputStream value = new ByteArrayInputStream(versionEntry.getBytes());
     
        when(servlet.getServletContext()).thenReturn(context);
        when(context.getResourceAsStream("/META-INF/MANIFEST.MF")).thenReturn(value);
        
        assertEquals("1.2.3", new ServletVersionComponent(servlet).getReport().getValue());
    }
}
