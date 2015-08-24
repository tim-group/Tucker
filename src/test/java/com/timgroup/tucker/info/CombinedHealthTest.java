package com.timgroup.tucker.info;

import org.junit.Test;

import static com.timgroup.tucker.info.Health.ALWAYS_HEALTHY;
import static com.timgroup.tucker.info.Health.State.healthy;
import static com.timgroup.tucker.info.Health.State.ill;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class CombinedHealthTest {
    private static final Health ALWAYS_ILL = new Health() {
        @Override
        public State get() {
            return ill;
        }
    };

    @Test
    public void
    reports_healthy_when_all_delegates_report_healthy() {
        assertThat(new CombinedHealth(ALWAYS_HEALTHY, ALWAYS_HEALTHY).get(), is(healthy));
    }

    @Test
    public void
    reports_ill_when_any_delegate_reports_ill() {
        assertThat(new CombinedHealth(ALWAYS_HEALTHY, ALWAYS_ILL).get(), is(ill));
        assertThat(new CombinedHealth(ALWAYS_ILL, ALWAYS_ILL).get(), is(ill));
    }
}