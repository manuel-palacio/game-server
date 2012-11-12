package com.palacesoft.server.scores;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * End point for posting scores to server
 *
 * @author Manuel Palacio
 */
public class ScoreResource extends HttpServlet {

    private ScoreService defaultScoreService;

    public ScoreResource(ScoreService defaultScoreService) {

        this.defaultScoreService = defaultScoreService;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String pathInfo = req.getPathInfo();
        int level = Integer.parseInt(pathInfo.substring(1, pathInfo.lastIndexOf("/")));

        try (BufferedReader bufferedReader = req.getReader()) {
            Integer userId = (Integer) req.getAttribute("userId");
            try {
                String line = bufferedReader.readLine();
                if (line != null) {
                    Integer score = Integer.parseInt(line);
                    ScoreInfo scoreInfo = new ScoreInfo(level, userId, score);
                    defaultScoreService.saveScore(scoreInfo);
                }
            } catch (IllegalArgumentException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        }
    }
}
