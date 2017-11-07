package com.timgroup.tucker.info.component;

import com.timgroup.tucker.info.Report;
import com.timgroup.tucker.info.component.FileDescriptorComponent.FileDescriptorProvider;
import org.junit.AssumptionViolatedException;
import org.junit.Test;

import static com.timgroup.tucker.info.Status.CRITICAL;
import static com.timgroup.tucker.info.Status.OK;
import static com.timgroup.tucker.info.Status.WARNING;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class FileDescriptorComponentTest {
    private FileDescriptorProvider fileDescriptorProvider = mock(FileDescriptorComponent.FileDescriptorProvider.class);
    private FileDescriptorComponent component = new FileDescriptorComponent(fileDescriptorProvider);
    
    @Test public void 
    whenUsedFileDescriptorsIsLessThanHalfStatusIsOk() {
        given(fileDescriptorProvider.used()).willReturn(15L);
        given(fileDescriptorProvider.total()).willReturn(100L);

        assertThat(component.getReport(), is(new Report(OK, "15/100 used")));
    }
    
    @Test public void 
    whenMoreThanHalfOfDescriptorsAreUsedStatusIsWarning() {
        given(fileDescriptorProvider.used()).willReturn(51L);
        given(fileDescriptorProvider.total()).willReturn(100L);

        assertThat(component.getReport(), is(new Report(WARNING, "51/100 used")));
    }
    
    @Test public void 
    whenMoreThanNinetyPercentOfDescriptorsAreUsedStatusIsCritical() {
        given(fileDescriptorProvider.used()).willReturn(91L);
        given(fileDescriptorProvider.total()).willReturn(100L);

        assertThat(component.getReport(), is(new Report(CRITICAL, "91/100 used")));
    }
    
    @Test public void
    fileDescriptorProviderGivesStats() {
        FileDescriptorProvider provider = FileDescriptorProvider.getDefault().orElseThrow(() -> new AssumptionViolatedException("No provider available on this platform"));

        assertTrue(provider.total() > 0);
        assertTrue(provider.used() > 0);
    }
}
