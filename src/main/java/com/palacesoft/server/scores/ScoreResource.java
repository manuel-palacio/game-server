package com.palacesoft.server.scores;


import java.io.BufferedReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static java.util.logging.Logger.*;

/**
 * End point for posting scores to server
 *
 * @author Manuel Palacio
 */
public class ScoreResource extends HttpServlet {


    private static final Logger logger = getLogger(ScoreResource.class.getPackage().getName());

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
            int score;
            try {
                score = Integer.parseInt(bufferedReader.readLine());
                ScoreInfo scoreInfo = new ScoreInfo(level, userId, score);
                defaultScoreService.saveScore(scoreInfo);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Could not save score", e);
            }
        }
    }
}
