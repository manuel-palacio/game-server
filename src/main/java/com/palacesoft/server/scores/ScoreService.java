package com.palacesoft.server.scores;


public interface ScoreService {
    byte [] getHighestScoresForLevel(int level);

    String getHighestScoresForLevelAsString(int level);

    void saveScore(ScoreInfo scoreInfo);

    void shutDown();
}
