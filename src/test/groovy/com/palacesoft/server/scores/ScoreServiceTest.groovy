package com.palacesoft.server.scores

import org.testng.annotations.BeforeTest
import org.testng.annotations.Test

import java.util.concurrent.Callable

import static com.jayway.awaitility.Awaitility.await
import static java.util.concurrent.TimeUnit.SECONDS
import org.testng.annotations.AfterTest

class ScoreServiceTest {

    def scoreService = new DefaultScoreService()

    @BeforeTest
    private void load() throws Exception {

        (1..10).each {
            scoreService.saveScore(new ScoreInfo(1, 666, 10))
            scoreService.saveScore(new ScoreInfo(1, 666, 9))
            scoreService.saveScore(new ScoreInfo(1, 777, 8))
            scoreService.saveScore(new ScoreInfo(1, 777, 8))
            scoreService.saveScore(new ScoreInfo(1, 777, 9))
            scoreService.saveScore(new ScoreInfo(1, 111, 8))
            scoreService.saveScore(new ScoreInfo(1, 111, 9))
            scoreService.saveScore(new ScoreInfo(1, 8989, 20))
            scoreService.saveScore(new ScoreInfo(1, 444, 100))
            scoreService.saveScore(new ScoreInfo(1, 333, 50))
        }

        scoreService.saveScore(new ScoreInfo(2, 333, 50))

        await() atMost(5, SECONDS) until([call: {scoreService.getHighestScoresForLevelAsString(1).split(",").length == 6}] as Callable)

    }

    @AfterTest
    public void shutdown() {
        scoreService.shutDown()
    }


    @Test
    public void get_highest_scores_for_level() throws Exception {
        assert scoreService.getHighestScoresForLevelAsString(1) == "444=100,333=50,8989=20,666=10,111=9,777=9"
        assert scoreService.getHighestScoresForLevelAsString(2) == "333=50"
    }
}
