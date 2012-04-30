package com.timgroup.status.servlet;

import java.io.IOException;
import java.io.OutputStream;

public interface WebResponse {
    
    public OutputStream respond(int status, String contentType, String characterEncoding) throws IOException;
    
}
