package com.timgroup.tucker.info.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.timgroup.tucker.info.ApplicationInformationHandler;
import com.timgroup.tucker.info.status.StatusPageGenerator;

/**
 * Serves requests for application information and supporting media.
 */
@SuppressWarnings("serial")
public class ApplicationInformationServlet extends HttpServlet {
    
    private final ApplicationInformationHandler handler;
    
    public ApplicationInformationServlet(StatusPageGenerator statusPage) {
        handler = new ApplicationInformationHandler(statusPage);
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        handler.handle(request.getPathInfo(), new ServletWebResponse(request, response));
    }
}
