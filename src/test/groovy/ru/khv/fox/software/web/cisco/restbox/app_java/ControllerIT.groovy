/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java

import io.restassured.RestAssured
import io.restassured.filter.log.RequestLoggingFilter
import io.restassured.filter.log.ResponseLoggingFilter
import io.restassured.http.ContentType
import org.apache.http.HttpStatus
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner

import static io.restassured.RestAssured.given
import static io.restassured.RestAssured.when
import static org.hamcrest.core.IsEqual.equalTo

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = [
        "security.user.name=user",
        "security.user.password=pass",
        "security.enable-csrf=true",
        "security.sessions=if_required"
])
class ControllerIT {

    // Default user is no longer configurable past Spring Boot 2.0M4
    // https://spring.io/blog/2017/09/15/security-changes-in-spring-boot-2-0-m4


    @TestConfiguration
    static class UserDetailsServiceConfiguration {
        @Bean
        @Primary
        UserDetailsService userDetailsService() throws Exception {
            final InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager()
            manager.createUser(User.withDefaultPasswordEncoder()
                    .username("user")
                    .password("pass")
                    .roles("USER")
                    .build())
            return manager
        }
    }

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
        // @formatter:off
        given()
          .accept(ContentType.JSON)
        .when()
          .get("/")
        .then()
          .statusCode(HttpStatus.SC_UNAUTHORIZED)
	// @formatter:on
    }

    @Test
    void 'api call with authentication must succeed'() {
        // @formatter:off
        given()
          .accept(ContentType.JSON)
          .auth().preemptive().basic("user", "pass")
        .when()
          .get("/")
        .then()
          .statusCode(HttpStatus.SC_OK)
	// @formatter:on
    }

    @Test
    void 'get css hello must succeed'() {
        // @formatter:off
        when()
            .get("css/hello")
        .then()
            .statusCode(HttpStatus.SC_OK)
	    // @formatter:on
    }

    @Test
    void 'POST without CSRF token must return 403'() {
        // @formatter:off
        given()
            .auth().preemptive().basic("user", "pass")
        .when()
            .post("/post")
        .then()
            .statusCode(HttpStatus.SC_FORBIDDEN)
	    // @formatter:on
    }

    @Test
    void 'passed x-custom-header must be returned'() {
        // @formatter:off
        def sessionCookie = given()
                .header("x-custom-header", "hello")
                .when()
                .get("customHeader")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED)
                .extract().cookie("JSESSIONID")

        given()
                .auth().basic("user", "pass")
                .cookie("JSESSIONID", sessionCookie)
                .when()
                .get("customHeader")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(equalTo("hello"))
        // @formatter:on
    }

    @Test
    void 'JSESSIONID must be changed after login'() {
        // @formatter:off
        def sessionCookie = when()
                .get("/")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED)
                .extract().cookie("JSESSIONID")

        def newCookie = given()
                .auth().basic("user", "pass")
                .cookie("JSESSIONID", sessionCookie)
                .when()
                .get("/")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().cookie("JSESSIONID")

        Assert.assertNotEquals(sessionCookie, newCookie)
        // @formatter:on
    }
}
