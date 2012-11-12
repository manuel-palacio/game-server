package com.palacesoft.server.scores

import org.testng.annotations.Test

import static com.jayway.restassured.RestAssured.expect
import static com.jayway.restassured.RestAssured.get
import static org.eclipse.jetty.http.HttpStatus.OK_200
import static org.hamcrest.CoreMatchers.equalTo

class ScoreIT extends AbstractIT {

    def sessionKey

    @Test
    public void login() {
        sessionKey = get("/666/login").asString()
        assert sessionKey
    }

    @Test(dependsOnMethods = "login")
    public void post_and_get_score() {
        expect() statusCode OK_200 with() body 600 when() post "/6/score?sessionKey=${sessionKey}"
        assert get("/6/highscorelist").asString() == "666=600"
    }

    @Test(dependsOnMethods = "login")
    public void post_with_no_score_does_nothing() {
        expect() statusCode OK_200 when() post "/6/score?sessionKey=${sessionKey}"

    }

    @Test
    public void level_with_no_scores_returns_empty_string() {
        expect() statusCode OK_200 body equalTo("") when() get "/0/highscorelist"
    }

    @Test(dependsOnMethods = "login")
    public void userId_appears_only_once_in_scores() {

        (1..10).each {
            expect() statusCode OK_200 with() body it when() post "/0/score?sessionKey=${sessionKey}"
        }
        assert get("/0/highscorelist").asString().split(",").length == 1
    }
}
