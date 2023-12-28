package com.example.next.study.test

import io.restassured.RestAssured
import org.junit.jupiter.api.BeforeEach
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class AbstractSprintBootTest() {
    @LocalServerPort
    var port: Int = 0
    
    @BeforeEach
    fun setUp() {
        RestAssured.port = port
    }
}
