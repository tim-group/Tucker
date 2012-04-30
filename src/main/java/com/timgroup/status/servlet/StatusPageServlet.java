package com.timgroup.status.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.timgroup.status.StatusPage;

/**
 * Serves requests for the status page, and for its supporting media.
 */
@SuppressWarnings("serial")
public class StatusPageServlet extends HttpServlet {
    
    private final StatusPageHandler handler = new StatusPageHandler();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        handler.handle(request.getPathInfo(), new ServletWebResponse(request, response));
    }
    
    public void setStatusPage(StatusPage statusPage) {
        handler.setStatusPage(statusPage);
    }
    
}
