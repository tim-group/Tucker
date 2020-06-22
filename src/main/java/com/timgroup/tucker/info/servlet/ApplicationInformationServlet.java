package com.timgroup.tucker.info.servlet;

import com.codahale.metrics.MetricRegistry;
import com.timgroup.metrics.MetricsWriter;
import com.timgroup.tucker.info.ApplicationInformationHandler;
import com.timgroup.tucker.info.Health;
import com.timgroup.tucker.info.LegacyMetricsWriter;
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

    public ApplicationInformationServlet(String applicationId, Stoppable stoppable, Health health, MetricsWriter metricsWriter) {
        this.statusPage = new StatusPageGenerator(applicationId, new ServletVersionComponent(this));
        this.handler = new ApplicationInformationHandler(statusPage, stoppable, health, metricsWriter);
        this.startupTimer = new StartupTimer(health);
    }

    public ApplicationInformationServlet(StatusPageGenerator statusPage, Stoppable stoppable, Health health, MetricsWriter metricsWriter) {
        this.statusPage = statusPage;
        this.handler = new ApplicationInformationHandler(statusPage, stoppable, health, metricsWriter);
        this.startupTimer = new StartupTimer(health);
    }

    /**
     * Use {@link Stoppable#ALWAYS_STOPPABLE} if you don't care about stoppable.
     *
     * @param statusPage Status page generator
     * @param stoppable  Indicator for stoppability
     * @param health     Indicator for application health
     */
    public ApplicationInformationServlet(StatusPageGenerator statusPage, Stoppable stoppable, Health health) {
        this(statusPage, stoppable, health, new MetricRegistry());
    }

    /**
     * Use {@link Stoppable#ALWAYS_STOPPABLE} if you don't care about stoppable.
     *
     * @param statusPage Status page generator
     * @param stoppable  Indicator for stoppability
     * @param health     Indicator for application health
     */
    public ApplicationInformationServlet(StatusPageGenerator statusPage, Stoppable stoppable, Health health, MetricRegistry metricRegistry) {
        this(statusPage, stoppable, health, new LegacyMetricsWriter(metricRegistry));
    }

    /**
     * Use {@link Stoppable#ALWAYS_STOPPABLE} if you don't care about stoppable.
     *
     * @param applicationId Application name
     * @param stoppable     Indicator for stoppability
     * @param health        Indicator for application health
     */
    public ApplicationInformationServlet(String applicationId, Stoppable stoppable, Health health) {
        this(applicationId, stoppable, health, new MetricRegistry());
    }

    /**
     * Use {@link Stoppable#ALWAYS_STOPPABLE} if you don't care about stoppable.
     *
     * @param applicationId Application name
     * @param stoppable     Indicator for stoppability
     * @param health        Indicator for application health
     */
    public ApplicationInformationServlet(String applicationId, Stoppable stoppable, Health health, MetricRegistry metricRegistry) {
        this(applicationId, stoppable, health, new LegacyMetricsWriter(metricRegistry));
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
        } else {
            handler.handle(path, webResponse);
        }
    }

    public static class Builder {
        private Stoppable stoppable = Stoppable.ALWAYS_STOPPABLE;
        private Health health = Health.ALWAYS_HEALTHY;
        private MetricsWriter metricsWriter;

        private StatusPageGenerator statusPage;
        private String applicationId;

        public Builder(StatusPageGenerator statusPage) {
            this.statusPage = statusPage;
        }

        public Builder(String applicationId) {
            this.applicationId = applicationId;
        }

        public Builder setStoppable(Stoppable stoppable) {
            this.stoppable = stoppable;
            return this;
        }

        public Builder setHealth(Health health) {
            this.health = health;
            return this;
        }

        public Builder setMetricRegistry(MetricRegistry metricRegistry) {
            this.metricsWriter = new LegacyMetricsWriter(metricRegistry);
            return this;
        }

        public Builder setMetricsWriter(MetricsWriter metricsWriter) {
            this.metricsWriter = metricsWriter;
            return this;
        }

        public ApplicationInformationServlet build() {
            if (statusPage != null) {
                return new ApplicationInformationServlet(statusPage, stoppable, health, metricsWriter);
            } else {
                return new ApplicationInformationServlet(applicationId, stoppable, health, metricsWriter);
            }
        }
    }
}
