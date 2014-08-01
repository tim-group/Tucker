package com.timgroup.tucker.info.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.timgroup.tucker.info.ApplicationInformationHandler;
import com.timgroup.tucker.info.Health;
import com.timgroup.tucker.info.Stoppable;
import com.timgroup.tucker.info.component.ServletVersionComponent;
import com.timgroup.tucker.info.status.ApplicationReportGenerator;
import com.timgroup.tucker.info.status.StatusPageGenerator;

/**
 * Serves requests for application information and supporting media.
 */
@SuppressWarnings("serial")
public class ApplicationInformationServlet extends HttpServlet {

    private final ApplicationInformationHandler handler;
    private final StatusPageGenerator statusPage;

    /**
     * Use Stoppable.ALWAYS_STOPPABLE if you don't care about stoppable.
     */
    public ApplicationInformationServlet(StatusPageGenerator statusPage, Stoppable stoppable, Health health) {
        this.statusPage = statusPage;
        this.handler = new ApplicationInformationHandler(statusPage, stoppable, health);
    }

    /**
     * Use Stoppable.ALWAYS_STOPPABLE if you don't care about stoppable.
     */
    public ApplicationInformationServlet(String applicationId, Stoppable stoppable, Health health) {
        this.statusPage = new StatusPageGenerator(applicationId, new ServletVersionComponent(this));
        this.handler = new ApplicationInformationHandler(statusPage, stoppable, health);
    }

    public final ApplicationReportGenerator getStatusPageGenerator() {
        return statusPage;
    }

    @Override
    protected final void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {
        handler.handle(request.getPathInfo(), new ServletWebResponse(request, response));
    }
}
