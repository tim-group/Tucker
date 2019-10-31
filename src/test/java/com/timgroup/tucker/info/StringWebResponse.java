package com.timgroup.tucker.info;

import com.google.common.base.Charsets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

class StringWebResponse implements WebResponse {
    public final Map<String, String> headers = new HashMap<>();
    public final ByteArrayOutputStream body = new ByteArrayOutputStream();

    public String contentType;
    public String characterEncoding;
    public int statusCode;
    public String relativePathRedirect;

    @Override
    public void setHeader(String name, String value) throws IOException {
        headers.put(name, value);
    }

    @Override
    public OutputStream respond(String contentType, String characterEncoding) throws IOException {
        this.contentType = contentType;
        this.characterEncoding = characterEncoding;
        return body;
    }

    @Override
    public void respond(int statusCode) throws IOException {
        this.statusCode = statusCode;
    }

    @Override
    public void reject(int status, String message) throws IOException {
        this.statusCode = status;
        body.write(message.getBytes(Charsets.UTF_8));
    }

    @Override
    public void redirect(String relativePath) throws IOException {
        statusCode = 302;
        this.relativePathRedirect = relativePath;
    }

    public String bodyString() {
        return new String(body.toByteArray(), Charsets.UTF_8);
    }
}
