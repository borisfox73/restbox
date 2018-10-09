/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java

import com.fasterxml.jackson.annotation.JsonProperty
import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtParser
import io.jsonwebtoken.Jwts
import io.restassured.RestAssured
import io.restassured.filter.log.RequestLoggingFilter
import io.restassured.filter.log.ResponseLoggingFilter
import io.restassured.http.ContentType
import org.apache.http.HttpStatus
import org.hamcrest.Matcher
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import ru.khv.fox.software.web.cisco.restbox.app_java.configuration.AppProperties
import ru.khv.fox.software.web.cisco.restbox.app_java.configuration.AppProperties.JwtProperties

import static io.restassured.RestAssured.given
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class IT20Authentication {

    private static final String LOGIN_ENDPOINT = "/login"
    private static final String USERINFO_ENDPOINT = "/userinfo"

	private static class LoginRequest {
		@JsonProperty
		private String username
		@JsonProperty
		private String password
	}

	@LocalServerPort
    private int serverPort
    @Autowired
    private AppProperties appProperties
    private LoginRequest loginRequest = new LoginRequest()


    private void invalidCredentials() {
//        loginRequest.setUsername("unknownuser")
//        loginRequest.setPassword("zzz")
	    loginRequest.username = "unknownuser"
	    loginRequest.password = "zzz"
    }

    private void validCredentials() {
//	    loginRequest.setUsername("testuser")
//	    loginRequest.setPassword("testpass")
	    loginRequest.username = "testuser"
	    loginRequest.password = "testpass"
    }


    @Before
    void initRestAssured() {
        RestAssured.port = serverPort
        RestAssured.filters(new ResponseLoggingFilter())
        RestAssured.filters(new RequestLoggingFilter())
    }

    @Test
    void 'unauthenticated api call'() {
        // @formatter:off
        given()
            .accept(ContentType.JSON)
        .when()
            .get(USERINFO_ENDPOINT)
        .then()
            .statusCode(HttpStatus.SC_UNAUTHORIZED)
            .body("error.status", is(401),
                  "error.reason", is("Access Denied: Not Authenticated"))
    	// @formatter:on
    }

    @Test
    void 'login with invalid credentials'() {
        invalidCredentials()
        // @formatter:off
        given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(loginRequest)
        .when()
            .post(LOGIN_ENDPOINT)
        .then()
            .statusCode(HttpStatus.SC_UNAUTHORIZED)
            .body("error.status", is(401),
                  "error.reason", is("Invalid Credentials"))
    	// @formatter:on
    }

    @Test
    void 'login and acquire jwt'() {
        validCredentials()
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
            .extract().path("token")
    	// @formatter:on
        println "jwt = $jwt"
        // verify jwt
        JwtProperties jwtProperties = appProperties.getJwt()
        JwtParser jwtParser = Jwts.parser()
                .setSigningKey(jwtProperties.getSecret().getBytes())
//                .setAllowedClockSkewSeconds(5L)
        jwtProperties.getAudience().ifPresent({ a -> jwtParser.requireAudience(a) })
        Claims jwtBody = jwtParser.parseClaimsJws(jwt).getBody()
        println "jwt claims: $jwtBody"
        UUID jwtId = UUID.fromString(jwtBody.getId())
        assertThat(jwtId, is(notNullValue(UUID.class)))
        assertThat(jwtBody.getSubject(), is("testuser"))
//	    assertThat(jwtBody.getIssuer(), is("http://localhost:$serverPort/".toString()))
	    assertThat(jwtBody.getIssuer(), is("http://localhost".toString()))
        Collection<String> authorities = jwtBody.get("authorities", Collection.class)
	    assertThat(authorities, contains("ROLE_USER") as Matcher<? super Collection>)
    }
}
