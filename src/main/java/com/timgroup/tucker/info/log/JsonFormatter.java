package com.timgroup.tucker.info.log;

import java.io.FilterWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.function.Consumer;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

public final class JsonFormatter {
    private static final JsonFactory JSON_FACTORY = new JsonFactory().enable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);

    public static JsonGenerator generate(Consumer<String> sink) throws IOException {
        StringWriter sw = new StringWriter();
        FilterWriter fw = new FilterWriter(sw) {
            @Override
            public void close() throws IOException {
                sink.accept(sw.toString());
            }
        };
        return JSON_FACTORY.createGenerator(fw);
    }

    private JsonFormatter() {
    }
}
