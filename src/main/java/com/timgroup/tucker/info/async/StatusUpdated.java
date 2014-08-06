package com.timgroup.tucker.info.async;

import com.timgroup.tucker.info.Report;

public interface StatusUpdated {
    void apply(Report report);

    public static final StatusUpdated NOOP = new StatusUpdated() {
        @Override
        public void apply(Report report) { }
    };
}