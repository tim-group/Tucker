package com.timgroup.tucker.info.servlet;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ServletWebResponse implements WebResponse {
    
    private final HttpServletRequest request;
    private final HttpServletResponse response;
    
    public ServletWebResponse(HttpServletRequest request, HttpServletResponse servletResponse) {
        this.request = request;
        this.response = servletResponse;
    }
    
    @Override
    public OutputStream respond(String contentType, String characterEncoding) throws IOException {
        response.setCharacterEncoding(characterEncoding);
        response.setContentType(contentType);
        return response.getOutputStream();
    }
    
    @Override
    public void reject(int status, String message) throws IOException {
        response.sendError(status, message);
    }
    
    @Override
    public void redirect(String relativePath) throws IOException {
        if (relativePath.startsWith("/")) {
            relativePath = request.getContextPath() + request.getServletPath() + relativePath;
        }
        response.sendRedirect(relativePath);
    }
    
}
