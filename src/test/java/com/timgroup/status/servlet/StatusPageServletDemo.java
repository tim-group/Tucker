package com.timgroup.status.servlet;

import java.io.IOException;

import com.timgroup.status.StatusPage;
import com.timgroup.status.ThresholdedGaugeComponent;
import com.timgroup.status.VersionComponent;
import com.yammer.metrics.core.Gauge;

public class StatusPageServletDemo {
    
    public static void main(String[] args) throws IOException {
        StatusPage statusPage = new StatusPage("demoApp");
        statusPage.addComponent(new VersionComponent(String.class));
        statusPage.addComponent(new ThresholdedGaugeComponent<Integer>("timeUsed", "Time used this minute (sec)", timeUsedGauge(), 30, 50));
        
        final StatusPageServlet servlet = new StatusPageServlet();
        servlet.setStatusPage(statusPage);
        
        ServletContainer container = new ServletContainer(servlet, 8888, "/status");
        container.start();
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
