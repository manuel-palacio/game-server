package com.palacesoft.server.scores;


public final class UserInfo {
    private final Integer userId;

    private final Long sessionStarted;

    public UserInfo(Integer userId, Long sessionStarted) {
        this.userId = userId;
        this.sessionStarted = sessionStarted;
    }

    public Long getSessionStarted() {
        return sessionStarted;
    }

    public Integer getUserId() {
        return userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UserInfo userInfo = (UserInfo) o;

        return sessionStarted.equals(userInfo.sessionStarted) && userId.equals(userInfo.userId);
    }

    @Override
    public int hashCode() {
        int result = userId;
        result = 31 * result + (int) (sessionStarted ^ (sessionStarted >>> 32));
        return result;
    }
}
