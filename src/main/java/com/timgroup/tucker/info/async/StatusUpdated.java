package com.timgroup.tucker.info.async;

import java.util.function.Consumer;

import com.timgroup.tucker.info.Report;

public interface StatusUpdated extends Consumer<Report> {
    StatusUpdated NOOP = report -> { };
}
