package com.timgroup.tucker.info.servlet;

import com.timgroup.tucker.info.WebResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

public class ServletWebResponse implements WebResponse {
    
    private final HttpServletRequest request;
    private final HttpServletResponse response;
    
    public ServletWebResponse(HttpServletRequest request, HttpServletResponse servletResponse) {
        this.request = request;
        this.response = servletResponse;
    }

    @Override
    public void setHeader(String name, String value) throws IOException {
        response.setHeader(name, value);
    }

    @Override
    public OutputStream respond(String contentType, String characterEncoding) throws IOException {
        response.setStatus(200);
        response.setCharacterEncoding(characterEncoding);
        response.setContentType(contentType);
        return response.getOutputStream();
    }

    @Override
    public void respond(int statusCode) throws IOException {
        response.setStatus(statusCode);
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
