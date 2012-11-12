package com.palacesoft.server.scores;

import java.util.EnumSet;

import javax.servlet.DispatcherType;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Starts embedded HTTP server
 *
 * @author Manuel Palacio
 */
public class Main {

    private Server server;

    public void startServer(boolean join) throws Exception {
        String webPort = System.getProperty("PORT");
        if (webPort == null || webPort.isEmpty()) {
            webPort = "8080";
        }
        server = new Server(Integer.valueOf(webPort));
        WebAppContext root = new WebAppContext(server, "/", "/");

        final ScoreService scoreService = new DefaultScoreService();
        root.addServlet(new ServletHolder(new ScoreResource(scoreService)), "/score/*");
        root.addServlet(new ServletHolder(new HighScoreResource(scoreService)), "/highscorelist/*");
        root.addFilter(new FilterHolder(new ScoreFilter()), "/*", EnumSet.of(DispatcherType.REQUEST));
        root.setParentLoaderPriority(true);
        server.setHandler(root);
        server.start();
        if (join) {
            server.join();
        }

        server.addLifeCycleListener(new AbstractLifeCycle.AbstractLifeCycleListener() {
            @Override
            public void lifeCycleStopped(LifeCycle event) {
                scoreService.shutDown();
            }
        });
    }

    public void stopServer() throws Exception {
        if (server != null) {
            server.setGracefulShutdown(0);
            server.stop();
        }
    }

    public static void main(String[] args) throws Exception {
        new Main().startServer(true);
    }
}
