package com.timgroup.tucker.info.httpserver;

import com.sun.net.httpserver.HttpServer;
import com.timgroup.tucker.info.ApplicationInformationHandler;
import com.timgroup.tucker.info.Health;
import com.timgroup.tucker.info.StartupTimer;
import com.timgroup.tucker.info.Stoppable;
import com.timgroup.tucker.info.status.StatusPageGenerator;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.Executors.newFixedThreadPool;

public class ApplicationInformationServer {

    public static ApplicationInformationServer create(int port, StatusPageGenerator statusPage, Health health) throws IOException {
        return ApplicationInformationServer.create(port, statusPage, Stoppable.ALWAYS_STOPPABLE, health);
    }

    public static ApplicationInformationServer create(int port, StatusPageGenerator statusPage, Stoppable stoppable, Health health) throws IOException {
        return create(port, new ApplicationInformationHandler(statusPage, stoppable, health), health);
    }

    private static ApplicationInformationServer create(int port, ApplicationInformationHandler handler, Health health) throws IOException {
        return new ApplicationInformationServer(port, handler, health);
    }

    private final String hostname;
    private final HttpServer server;
    private final StartupTimer startupTimer;

    private ApplicationInformationServer(int port, ApplicationInformationHandler handler, Health health) throws IOException {
        this.hostname = defaultHostname();
        URI potentialBaseUri = URI.create(String.format("http://%s:%d/info", hostname, port));
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext(potentialBaseUri.getPath(), new ApplicationInformationHttpHandler(handler, potentialBaseUri));
        server.setExecutor(newFixedThreadPool(5, new TuckerThreadFactory()));
        startupTimer = new StartupTimer(health);
    }

    private static String defaultHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (IOException e) {
            return "localhost";
        }
    }

    public URI getBase() {
        return URI.create(String.format("http://%s:%d/info", hostname, server.getAddress().getPort()));
    }

    public void start() {
        startupTimer.start();
        server.start();
    }

    public void stop() {
        startupTimer.stop();
        server.stop(0);
    }

    private static class TuckerThreadFactory implements ThreadFactory {
        final AtomicInteger threadNumber = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, "Tucker-" + threadNumber.getAndIncrement());
            thread.setDaemon(false);
            return thread;
        }
    }
}
