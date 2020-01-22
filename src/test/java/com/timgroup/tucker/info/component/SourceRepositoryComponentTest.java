package com.timgroup.tucker.info.component;

import com.timgroup.tucker.info.Status;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class SourceRepositoryComponentTest {

    private final ClassLoader classLoader = mock(ClassLoader.class);
    
    @Test
    public void default_id_and_label_are_suitable() throws Exception {
        assertEquals("sourcerepository", new SourceRepositoryComponent(classLoader).getId());
        assertEquals("Source Repository", new SourceRepositoryComponent(classLoader).getLabel());
    }

    @Test
    public void report_status_is_info() throws Exception {
        assertEquals(Status.INFO, new SourceRepositoryComponent(classLoader).getReport().getStatus());
    }

    @Test
    public void reports_source_repository() throws Exception {
        when(classLoader.getResource("META-INF/MANIFEST.MF")).thenReturn(getClass().getResource("example.manifest"));
        SourceRepositoryComponent component = new SourceRepositoryComponent(classLoader);

        assertEquals("git@git.net.local:the-repository.git",
                     String.valueOf(component.getReport().getValue()));
    }

    @Test
    public void reports_error_with_invalid_manifest() throws Exception {
        when(classLoader.getResource("META-INF/MANIFEST.MF")).thenReturn(getClass().getResource("broken.manifest"));
        SourceRepositoryComponent component = new SourceRepositoryComponent(classLoader);

        assertThat(String.valueOf(component.getReport().getValue()), containsString("Unable to read manifest: "));
    }

    @Test
    public void reports_error_with_missing_manifest() throws Exception {
        when(classLoader.getResource("META-INF/MANIFEST.MF")).thenReturn(null);
        SourceRepositoryComponent component = new SourceRepositoryComponent(classLoader);

        assertEquals("Source repository information not found",
                     String.valueOf(component.getReport().getValue()));
    }

    @Test
    public void reports_error_when_source_repository_information_not_found() throws Exception {
        SourceRepositoryComponent component = new SourceRepositoryComponent(classLoader);
        assertEquals("Source repository information not found",
                     String.valueOf(component.getReport().getValue()));
    }
}
