package com.palacesoft.server.scores;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * End point for fetching scores from server
 *
 * @author Manuel Palacio
 */
public class HighScoreResource extends HttpServlet {
    private ScoreService scoreService;

    public HighScoreResource(ScoreService scoreService) {
        this.scoreService = scoreService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        int level = Integer.parseInt(pathInfo.substring(1, pathInfo.lastIndexOf("/")));
        byte [] scores = scoreService.getHighestScoresForLevel(level);  //use stream since the content is already encoded correctly
        if (scores != null) {
            resp.setContentType("text/plain");
            resp.setContentLength(scores.length);
            resp.getOutputStream().write(scores, 0, scores.length);
        }
    }
}
