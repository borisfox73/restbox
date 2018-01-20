/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java

import io.restassured.RestAssured
import io.restassured.filter.log.RequestLoggingFilter
import io.restassured.filter.log.ResponseLoggingFilter
import org.apache.http.HttpStatus
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner

import static io.restassured.RestAssured.when

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "security.user.password=pass")
class ControllerIT {

    @LocalServerPort
    private int serverPort

    @Before
    void initRestAssured() {
        RestAssured.port = serverPort
        RestAssured.filters(new ResponseLoggingFilter())
        RestAssured.filters(new RequestLoggingFilter())
    }

    @Test
    void 'api call without authentication must fail'() {
        when()
                .get("/")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED)
    }
}
