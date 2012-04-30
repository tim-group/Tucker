package com.timgroup.status.servlet;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

public class ServletWebResponse implements WebResponse {
    
    private final HttpServletResponse response;
    
    public ServletWebResponse(HttpServletResponse servletResponse) {
        this.response = servletResponse;
    }
    
    @Override
    public OutputStream respond(String contentType, String characterEncoding) throws IOException {
        response.setCharacterEncoding(characterEncoding);
        response.setContentType(contentType);
        return response.getOutputStream();
    }
    
}
