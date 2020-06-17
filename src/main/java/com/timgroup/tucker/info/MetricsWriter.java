package com.timgroup.tucker.info;

import java.io.IOException;
import java.io.Writer;

public interface MetricsWriter {

    void writeMetrics(Writer writer) throws IOException;


}
