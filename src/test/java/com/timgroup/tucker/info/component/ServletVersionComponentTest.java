package com.timgroup.tucker.info.component;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.junit.Before;
import org.junit.Test;

import com.timgroup.tucker.info.Status;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ServletVersionComponentTest {

    private final ServletConfig config = mock(ServletConfig.class);
    private final ServletContext context = mock(ServletContext.class);

    @Before
    public void setUp() {
        when(config.getServletContext()).thenReturn(context);
    }

    @Test
    public void defaultIdAndLabelAreSuitable() throws Exception {
        assertEquals("version", new ServletVersionComponent(config).getId());
        assertEquals("Version", new ServletVersionComponent(config).getLabel());
    }

    @Test
    public void reportStatusIsInfo() throws Exception {
        assertEquals(Status.INFO, new ServletVersionComponent(config).getReport().getStatus());
    }

    @Test
    public void reportValueIsImplementationVersionOfPackageContainingAnchorClass() throws Exception {
        when(context.getResource("/META-INF/MANIFEST.MF")).thenReturn(getClass().getResource("example.manifest"));

        assertEquals("1.2.3", new ServletVersionComponent(config).getReport().getValue());
    }

    @Test
    public void reportHasNoValueIfExceptionThrown() throws Exception {
        when(context.getResourceAsStream("/META-INF/MANIFEST.MF")).thenThrow(new RuntimeException("Failed"));

        assertFalse(new ServletVersionComponent(config).getReport().hasValue());
    }

    @Test
    public void contextIsNotAccessedUntilVersionIsRequested() throws Exception {
        when(config.getServletContext()).thenThrow(new IllegalStateException("this doesn't work in some containers"));

        new ServletVersionComponent(config);
    }

}
