package com.timgroup.status.demo;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import com.timgroup.status.StatusPage;
import com.timgroup.status.ThresholdedGaugeComponent;
import com.timgroup.status.VersionComponent;
import com.timgroup.status.servlet.ApplicationInformationServlet;
import com.yammer.metrics.core.Gauge;

@SuppressWarnings("serial")
public class DemoStatusPageServlet extends ApplicationInformationServlet {
    
    public DemoStatusPageServlet() {
        super(statusPage());
    }
    
    private static StatusPage statusPage() {
        final StatusPage statusPage = new StatusPage("demoApp");
        statusPage.addComponent(new VersionComponent(StatusPage.class));
        statusPage.addComponent(new ThresholdedGaugeComponent<Integer>("timeUsed", "Time used this minute (sec)", timeUsedGauge(), 30, 50));
        return statusPage;
    }
    
    private static Gauge<Integer> timeUsedGauge() {
        return new Gauge<Integer>() {
            @Override
            public Integer value() {
                return (int) (System.currentTimeMillis() / 1000) % 60;
            }
        };
    }
    
}
