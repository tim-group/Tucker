package com.timgroup.tucker.info.servlet;

import com.timgroup.tucker.info.ApplicationInformationHandler;
import com.timgroup.tucker.info.Health;
import com.timgroup.tucker.info.StartupTimer;
import com.timgroup.tucker.info.Stoppable;
import com.timgroup.tucker.info.component.ServletVersionComponent;
import com.timgroup.tucker.info.status.StatusPageGenerator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Serves requests for application information and supporting media.
 */
@SuppressWarnings("serial")
public class ApplicationInformationServlet extends HttpServlet {

    private final ApplicationInformationHandler handler;
    private final StatusPageGenerator statusPage;
    private final StartupTimer startupTimer;

    /**
     * Use {@link Stoppable#ALWAYS_STOPPABLE} if you don't care about stoppable.
     *
     * @param statusPage Status page generator
     * @param stoppable Indicator for stoppability
     * @param health Indicator for application health
     */
    public ApplicationInformationServlet(StatusPageGenerator statusPage, Stoppable stoppable, Health health) {
        this.statusPage = statusPage;
        this.handler = new ApplicationInformationHandler(statusPage, stoppable, health);
        this.startupTimer = new StartupTimer(health);
    }

    /**
     * Use {@link Stoppable#ALWAYS_STOPPABLE} if you don't care about stoppable.
     *
     * @param applicationId Application name
     * @param stoppable Indicator for stoppability
     * @param health Indicator for application health
     */
    public ApplicationInformationServlet(String applicationId, Stoppable stoppable, Health health) {
        this.statusPage = new StatusPageGenerator(applicationId, new ServletVersionComponent(this));
        this.handler = new ApplicationInformationHandler(statusPage, stoppable, health);
        this.startupTimer = new StartupTimer(health);
    }

    public final StatusPageGenerator getStatusPageGenerator() {
        return statusPage;
    }

    @Override
    public void init() throws ServletException {
        startupTimer.start();
    }

    @Override
    public void destroy() {
        startupTimer.stop();
    }

    @Override
    protected final void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {
        String path = request.getPathInfo();
        String callback = request.getParameter("callback");
        ServletWebResponse webResponse = new ServletWebResponse(request, response);
        if (callback != null) {
            handler.handleJSONP(path, callback, webResponse);
        }
        else {
            handler.handle(path, webResponse);
        }
    }
}
