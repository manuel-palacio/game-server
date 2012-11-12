package com.palacesoft.server.scores;


import java.util.Date;

public final class ScoreInfo implements Comparable<ScoreInfo> {
    private final Integer level;

    private final Integer userId;

    private final Integer score;

    private Date dateCreated;

    public ScoreInfo(Integer level, Integer userId, Integer score) {

        this.level = level;
        this.userId = userId;
        this.score = score;
        this.dateCreated = new Date();
    }


    public Date getDateCreated() {
        return new Date(dateCreated.getTime());
    }

    public Integer getLevel() {
        return level;
    }

    public Integer getUserId() {
        return userId;
    }

    public Integer getScore() {
        return score;
    }

    @Override
    public int compareTo(ScoreInfo other) {
        final int BEFORE = -1;
        final int EQUAL = 0;
        final int AFTER = 1;

        if (this == other) {
            return EQUAL;
        }

        if (this.score > other.getScore()) {
            return BEFORE;

        }

        if (this.score < other.getScore()) {
            return AFTER;
        }

        if (!this.userId.equals(other.getUserId()) && this.score.equals(other.getScore())) {
            return AFTER;     //in case same score is submitted by two different users
        }

        return EQUAL;
    }


    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        ScoreInfo scoreInfo = (ScoreInfo) other;

        return level.equals(scoreInfo.level) && score.equals(scoreInfo.score) && userId.equals(scoreInfo.userId);

    }

    @Override
    public int hashCode() {
        int result = level;
        result = 31 * result + userId;
        result = 31 * result + score;
        return result;
    }

    @Override
    public String toString() {
        return "ScoreInfo{" +
                "level=" + level +
                ", userId=" + userId +
                ", score=" + score +
                '}';
    }
}
