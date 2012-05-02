package com.timgroup.tucker.info.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.timgroup.tucker.info.ApplicationInformationHandler;
import com.timgroup.tucker.info.component.ServletVersionComponent;
import com.timgroup.tucker.info.status.StatusPageGenerator;

/**
 * Serves requests for application information and supporting media.
 */
@SuppressWarnings("serial")
public class ApplicationInformationServlet extends HttpServlet {
    
    private final ApplicationInformationHandler handler;
    private final StatusPageGenerator statusPage;
    
    public ApplicationInformationServlet(StatusPageGenerator statusPage) {
        this.statusPage = statusPage;
        this.handler = new ApplicationInformationHandler(statusPage);
    }
    
    public ApplicationInformationServlet(String applicationId) {
        this.statusPage = new StatusPageGenerator(applicationId, new ServletVersionComponent(this));
        this.handler = new ApplicationInformationHandler(statusPage);
    }
    
    public final StatusPageGenerator getStatusPageGenerator() {
        return statusPage;
    }
    
    @Override
    protected final void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        handler.handle(request.getPathInfo(), new ServletWebResponse(request, response));
    }
}
