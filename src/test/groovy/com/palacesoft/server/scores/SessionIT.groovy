package com.palacesoft.server.scores

import org.testng.annotations.Test

import static com.jayway.restassured.RestAssured.expect
import static com.jayway.restassured.RestAssured.get
import static org.eclipse.jetty.http.HttpStatus.OK_200

import static org.eclipse.jetty.http.HttpStatus.*

class SessionIT extends AbstractIT {

    @Test
    public void should_get_400_when_session_expires() {
        String sessionKey = get("/567/login").asString()
        assert sessionKey
        Thread.sleep(4000)
        expect() statusCode FORBIDDEN_403 with() body 50 when() post "/1/score?sessionKey=${sessionKey}"
    }

    @Test
    public void should_get_200_when_posting_with_valid_session() {
        String sessionKey = get("/567/login").asString()
        assert sessionKey
        expect() statusCode OK_200 with() body 50 when() post "/1/score?sessionKey=${sessionKey}"
    }

    @Test
    public void should_get_400_when_posting_with_no_session() {
        expect() statusCode FORBIDDEN_403 with() body 50 when() post "/1/score"
    }
}
