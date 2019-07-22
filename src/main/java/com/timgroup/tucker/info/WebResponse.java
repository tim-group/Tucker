package com.timgroup.tucker.info;

import java.io.IOException;
import java.io.OutputStream;

public interface WebResponse {

    void setHeader(String name, String value) throws IOException;

    OutputStream respond(String contentType, String characterEncoding) throws IOException;

    void respond(int statusCode) throws IOException;

    void reject(int status, String message) throws IOException;
    
    void redirect(String relativePath) throws IOException;
    
}
