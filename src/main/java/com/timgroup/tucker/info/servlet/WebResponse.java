package com.timgroup.tucker.info.servlet;

import java.io.IOException;
import java.io.OutputStream;

public interface WebResponse {
    
    public OutputStream respond(String contentType, String characterEncoding) throws IOException;
    
    public void reject(int status, String message) throws IOException;
    
    public void redirect(String relativePath) throws IOException;
    
}
