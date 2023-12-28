package com.example.next.study.servlet

import com.example.next.study.test.AbstractSprintBootTest
import io.restassured.RestAssured
import org.junit.jupiter.api.Test

internal class CreateUserServletTest : AbstractSprintBootTest() {

    @Test
    fun `사용자 생성`() {
        // given
        val name = "next-step"
        val email = "test@naver.com"
        val password = "1234"
        val userId = 1L
        RestAssured.given().log().all()
            .param("name", name)
            .param("email", email)
            .param("password", password)
            .param("userId", userId)
            .`when`()
            .post("/users/create")
            .then().log().all()
            .statusCode(302)
            .header("Location", "/")
    }
}
