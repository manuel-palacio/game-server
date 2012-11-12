package com.palacesoft.server.scores;


import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Keeps track of user sessions and makes sure they expire after a certain period.
 * Forwards requests to servlet if needed
 * Rejects post score request if user does not have a valid session.
 *
 * @author Manuel Palacio
 */
public class ScoreFilter implements Filter {

    private ConcurrentHashMap<String, UserInfo> userSessions = new ConcurrentHashMap<>(8, 0.9f, 1);

    private ScheduledExecutorService executor;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        executor = Executors.newSingleThreadScheduledExecutor();
        final long periodMillis = Long.parseLong(
                System.getProperty("CLEANUP_PERIOD_MILLIS") != null ? System.getProperty("CLEANUP_PERIOD_MILLIS") : "600000");

        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                Set<String> keys = userSessions.keySet();
                for (String key : keys) {
                    long sessionStartTime = userSessions.get(key).getSessionStarted();
                    if ((now - sessionStartTime) > periodMillis) {
                        userSessions.remove(key);
                    }
                }
            }
        }, 1000, periodMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String uri = request.getRequestURI();

        switch (uri.substring(uri.lastIndexOf("/"))) {
            case "/login":
                int userId = Integer.parseInt(uri.substring(1, uri.lastIndexOf("/")));
                byte[] bytes = getSessionKey(userId).getBytes();
                response.setContentType("text/plain");
                response.setContentLength(bytes.length);
                response.getOutputStream().write(bytes, 0, bytes.length);
                break;
            case ("/score"):
                String sessionKey = request.getParameter("sessionKey");
                if (sessionKey == null || sessionKey.equals("")) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Session key must be provided");
                    return;
                }
                if (userSessions.containsKey(sessionKey)) {
                    request.setAttribute("userId", getUser(sessionKey).getUserId());
                    request.getRequestDispatcher("/score" + uri).forward(servletRequest, servletResponse);
                } else {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Session key has expired");
                }
                break;
            case ("/highscorelist"):
                request.getRequestDispatcher("/highscorelist" + uri).forward(servletRequest, servletResponse);
                break;
            default:
                filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    private UserInfo getUser(String userSession) {
        return userSessions.get(userSession);
    }

    private String getSessionKey(int userId) {
        long currentTime = System.currentTimeMillis();
        String sessionKey = UUID.randomUUID().toString();
        UserInfo userInfo = new UserInfo(userId, currentTime);
        userSessions.putIfAbsent(sessionKey, userInfo);
        return sessionKey;
    }


    @Override
    public void destroy() {
        executor.shutdown();
    }
}
