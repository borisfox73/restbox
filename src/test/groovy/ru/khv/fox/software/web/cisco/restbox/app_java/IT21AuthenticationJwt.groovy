/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java

import com.fasterxml.jackson.annotation.JsonProperty
import io.restassured.RestAssured
import io.restassured.filter.log.RequestLoggingFilter
import io.restassured.filter.log.ResponseLoggingFilter
import io.restassured.http.ContentType
import org.apache.http.HttpStatus
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner

import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.*

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = [
//        "app.config.users[0].username=testuser",
//        "app.config.users[0].password=testpass",
//        "app.config.users[0].roles=USER,ADMIN",
        "app.config.jwt.time-to-live=PT1M"
])
@ActiveProfiles("test")
class IT21AuthenticationJwt {

    private static final String LOGIN_ENDPOINT = "/login"
    private static final String USERINFO_ENDPOINT = "/userinfo"
	private static final String JSONTEST_ENDPOINT = "/jsontest"
    private static
    final String TAMPERED_JWT = "eyJhbGciOiJIUzUxMiJ9.eyJqdGkiOiJmYWNjZTNkOC0yM2FjLTQ3MWUtOGFiOC0xMTc4NzllMTg0MWUiLCJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTUyNjU5OTYyNCwibmJmIjoxNTI2NTk5NjI0LCJleHAiOjE1MjY1OTk5MjQsImF1dGhvcml0aWVzIjpbIlJPTEVfVVNFUiJdLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjUxNTk4LyIsImF1ZCI6InJlc3Rib3hfamF2YSJ9.kNInWEE4ux93yaOHafqfseiEBmK-r1_q9nlpk6oCqkbV6xvzm1sT12eEMbLG2ITrvnWmSHRJHoPHmT6vhhrS_Z"
    private static
    final String EXPIRED_JWT = "eyJhbGciOiJIUzUxMiJ9.eyJqdGkiOiJmYWNjZTNkOC0yM2FjLTQ3MWUtOGFiOC0xMTc4NzllMTg0MWUiLCJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTUyNjU5OTYyNCwibmJmIjoxNTI2NTk5NjI0LCJleHAiOjE1MjY1OTk5MjQsImF1dGhvcml0aWVzIjpbIlJPTEVfVVNFUiJdLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjUxNTk4LyIsImF1ZCI6InJlc3Rib3hfamF2YSJ9.kNInWEE4ux93yaOHafqfseiEBmK-r1_q9nlpk6oCqkbV6xvzm1sT12eEMbLG2ITrvnWmSHRJHoPHmT6vhhrS_A"

	private static class LoginRequest {
		@JsonProperty
		private String username
		@JsonProperty
		private String password

		LoginRequest(final String username, final String password) {
			this.username = username
			this.password = password
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

    private static String acquireJwt(String username, String password) {
	    LoginRequest loginRequest = new LoginRequest(username, password)
        // @formatter:off
        String jwt = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(loginRequest)
        .when()
            .post(LOGIN_ENDPOINT)
        .then()
            .statusCode(HttpStatus.SC_CREATED)
            .body("token", is(notNullValue(String.class)))
                .extract()
                    .path("token")
    	// @formatter:on
        println "jwt = $jwt"
        return jwt
    }

    @Test
    void 'user info 1'() {
        String user = "testuser"
        String jwt = acquireJwt(user, "testpass")
        // @formatter:off
        given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .auth().preemptive()
                .oauth2(jwt)
            .when()
                .get(USERINFO_ENDPOINT)
            .then()
                .statusCode(HttpStatus.SC_OK)
                .body("username", equalTo(user),
                "authorities.authority", contains("ROLE_USER") )
    	// @formatter:on
    }

    @Test
    void 'user info 2'() {
        String user = "test2"
        String jwt = acquireJwt(user, "pass2")
        // @formatter:off
        given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .auth().preemptive()
                .oauth2(jwt)
            .when()
                .get(USERINFO_ENDPOINT)
            .then()
                .statusCode(HttpStatus.SC_OK)
                .body("username", equalTo(user),
                "authorities.authority", containsInAnyOrder("ROLE_ADMIN", "ROLE_OTHER") )
    	// @formatter:on
    }

	@Test
	void 'json test 1'() {
		String user = "testuser"
		String jwt = acquireJwt(user, "testpass")
		// @formatter:off
        given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .auth().preemptive()
                .oauth2(jwt)
            .when()
                .get(JSONTEST_ENDPOINT)
            .then()
                .statusCode(HttpStatus.SC_FORBIDDEN)
                .body("message", is("access denied error"),
                "error.path", is("/jsontest"),
                "error.status", is(403),
                "error.error", is("Forbidden"),
                "error.reason", is("Access Denied"))
    	// @formatter:on
	}

	@Test
	void 'json test 2'() {
		String user = "test2"
		String jwt = acquireJwt(user, "pass2")
		// @formatter:off
        given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .auth().preemptive()
                .oauth2(jwt)
            .when()
                .get(JSONTEST_ENDPOINT)
            .then()
                .statusCode(HttpStatus.SC_OK)
                .body("authenticated", is(true),
		        "name", equalTo(user),
		        "principal.username", equalTo(user),
                "principal.authorities.authority", containsInAnyOrder("ROLE_ADMIN", "ROLE_OTHER") )
    	// @formatter:on
	}

	@Test
    void 'tampered jwt'() {
        // @formatter:off
        given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .auth().preemptive()
                .oauth2(TAMPERED_JWT)
            .when()
                .get(USERINFO_ENDPOINT)
            .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED)
                .body("error.status", is(401),
                  "error.reason", is("JWT signature does not match locally computed signature. JWT validity cannot be asserted and should not be trusted."))
    	// @formatter:on
    }

    @Test
    void 'expired jwt'() {
        // @formatter:off
        given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .auth().preemptive()
                .oauth2(EXPIRED_JWT)
            .when()
                .get(USERINFO_ENDPOINT)
            .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED)
                .body("error.status", is(401),
                  "error.reason", startsWith("JWT expired at 2018-05-18T09:32:04Z."))
    	// @formatter:on
    }
}
