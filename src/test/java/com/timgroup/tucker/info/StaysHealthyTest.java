package com.timgroup.tucker.info;

import com.timgroup.tucker.info.Health.State;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class StaysHealthyTest {

    @Test public void
    stays_healthy_once_healthy() throws Exception {
        State[] states = new State[]{State.ill};
        Health health = StaysHealthy.onceHealthy(() -> states[0]);
        assertThat(health.get(), is(State.ill));
        states[0] = State.healthy;
        assertThat(health.get(), is(State.healthy));
        states[0] = State.ill;
        assertThat(health.get(), is(State.healthy));
    }

}
