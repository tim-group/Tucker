package com.timgroup.tucker.info.component;

import static com.timgroup.tucker.info.Status.CRITICAL;
import static com.timgroup.tucker.info.Status.OK;
import static com.timgroup.tucker.info.Status.WARNING;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import org.junit.Test;

import com.timgroup.tucker.info.Report;
import com.timgroup.tucker.info.component.PermGenComponent.PermGenProvider;

public class PermGenComponentTest {

    @Test public void
    whenUsedPermGenIsLessThanEightyPercentStatusIsOk() {
        PermGenProvider permGenProvider = mock(PermGenProvider.class);
        PermGenComponent permGenComponent = new PermGenComponent(permGenProvider);
        
        given(permGenProvider.used()).willReturn(79L);
        given(permGenProvider.total()).willReturn(100L);
        
        assertThat(permGenComponent.getReport(), is(new Report(OK, "79% used")));
    }
    
    @Test public void
    whenUsedPermGenIsMoreThanEightyPercentStatusIsWarning() {
        PermGenProvider permGenProvider = mock(PermGenProvider.class);
        PermGenComponent permGenComponent = new PermGenComponent(permGenProvider);
        
        given(permGenProvider.used()).willReturn(81L);
        given(permGenProvider.total()).willReturn(100L);
        
        assertThat(permGenComponent.getReport(), is(new Report(WARNING, "81% used")));
    }
    
    @Test public void
    whenUsedPermGenIsMoreThanNinetyFivePercentStatusIsCritical() {
        PermGenProvider permGenProvider = mock(PermGenProvider.class);
        PermGenComponent permGenComponent = new PermGenComponent(permGenProvider);
        
        given(permGenProvider.used()).willReturn(96L);
        given(permGenProvider.total()).willReturn(100L);
        
        assertThat(permGenComponent.getReport(), is(new Report(CRITICAL, "96% used")));
    }
    
    @Test public void
    permGenProviderGivesStats() {
        assumeTrue(System.getProperty("java.version").compareTo("1.8") < 0);

        PermGenProvider provider = new PermGenComponent.PermGenProvider();
        
        assertTrue(provider.total() > 0);
        assertTrue(provider.used() > 0);
    }
}
