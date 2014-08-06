package com.timgroup.tucker.info.async;

import java.util.Date;

public interface Clock {
    Date now();

    public static class SystemClock implements Clock {
        @Override
        public Date now() {
            return new Date();
        }
    }

}