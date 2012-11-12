package com.palacesoft.server.scores

import org.testng.annotations.BeforeClass
import org.testng.annotations.AfterClass

abstract class AbstractIT {

    def main = new Main()

    @BeforeClass
    public void init() {
        main.startServer(false)
    }

    @AfterClass
    public void stop() {
        main.stopServer()
    }
}
