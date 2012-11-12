package com.palacesoft.server.scores

import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

import static com.jayway.restassured.RestAssured.expect
import static com.jayway.restassured.RestAssured.get
import static org.eclipse.jetty.http.HttpStatus.OK_200
import groovyx.gpars.GParsPool

class StressIT extends AbstractIT {

    def expectations = [:]

    def random = new Random()

    def expectationsCsv

    @BeforeClass
    def createExpectations() {
        def userSession = [:]
        GParsPool.withPool {
            (1..50).eachParallel {
                level ->
                def sessionAndScore = [:]
                (1..50).each {
                    customerId ->
                    String sessionKey
                    if (!userSession[customerId]) {
                        sessionKey = get("/${customerId}/login").asString()
                        userSession[customerId] = sessionKey
                    } else {
                        sessionKey = userSession[customerId]
                    }
                    int score = random.nextInt(10000)
                    sessionAndScore[sessionKey] = new ScoreInfo(level, customerId, score)
                }
                expectations[level] = sessionAndScore
            }
        }
    }

    @Test
    public void post_scores() {
        GParsPool.withPool {
            expectations.eachParallel {
                def sessionAndScores = it.value
                sessionAndScores.each {
                    String sessionKey = it.key
                    def scores = it.value
                    scores.each {
                        expect() statusCode OK_200 with() body it.score when() post "/${it.level}/score?sessionKey=${sessionKey}"
                    }
                }
            }
        }
    }


    @Test(dependsOnMethods = "post_scores")
    public void verify_scores() {
        expectationsCsv = expectationsAsCsv()
        GParsPool.withPool {
            expectationsCsv.eachParallel {
                def scores = get("/${it.key}/highscorelist").asString()
                assert scores == it.value
            }
        }
    }

    @Test(dependsOnMethods = "verify_scores", threadPoolSize = 4, invocationCount = 1000)
    public void get_scores_parallel_and_random() {
        def randomKey = random.nextInt(expectationsCsv.keySet().size())
        if(randomKey == 0){
            randomKey = 1
        }
        def scores = get("/${randomKey}/highscorelist").asString()
        assert scores == expectationsCsv.get(randomKey)
    }

    private def expectationsAsCsv() {
        def levelAndCvsScores = [:]
        expectations.each {
            def level = it.key
            def sessionAndScore = it.value
            def results = [] as TreeSet
            sessionAndScore.each {
                results << it.value
            }
            def cvs = []
            results.toList()[0..14].each {
                cvs << """${it.userId}=${it.score}"""
            }
            levelAndCvsScores[level] = cvs.join(",")
        }
        levelAndCvsScores
    }
}
