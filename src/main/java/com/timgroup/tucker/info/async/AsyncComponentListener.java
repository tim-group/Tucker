package com.timgroup.tucker.info.async;

import java.util.function.BiConsumer;

import com.timgroup.tucker.info.Report;

public interface AsyncComponentListener extends BiConsumer<AsyncComponent, Report> {
}
