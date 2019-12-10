/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.integrationTests

import com.fasterxml.jackson.annotation.JsonProperty
import io.jsonwebtoken.Jwts
import io.restassured.RestAssured
import io.restassured.builder.RequestSpecBuilder
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.filter.log.LogDetail
import io.restassured.filter.log.RequestLoggingFilter
import io.restassured.filter.log.ResponseLoggingFilter
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import io.restassured.specification.ResponseSpecification
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
	private static RequestSpecification reqSpecBase
	private static ResponseSpecification respSpecBase


	private void emptyCredentials() {
		loginRequest.username = ""
		loginRequest.password = null
	}

	private void invalidCredentials() {
	    loginRequest.username = "unknownuser"
	    loginRequest.password = "zzz"
    }

    private void validCredentials() {
	    loginRequest.username = "testuser"
	    loginRequest.password = "testpass"
    }


    @Before
    void initRestAssured() {
	    if (reqSpecBase == null) {
		    RestAssured.port = serverPort
		    RestAssured.filters(new ResponseLoggingFilter())
		    RestAssured.filters(new RequestLoggingFilter())
		    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails()
		    reqSpecBase = new RequestSpecBuilder()
				    .setContentType(ContentType.JSON)
				    .setAccept(ContentType.JSON)
				    .log(LogDetail.ALL)
				    .build()
		    respSpecBase = new ResponseSpecBuilder()
				    .expectContentType(ContentType.JSON)
				    .build()
	    }
    }

    @Test
    void 'unauthenticated api call'() {
        // @formatter:off
        given()
            .spec(reqSpecBase)
        .when()
            .get(USERINFO_ENDPOINT)
        .then()
            .spec(respSpecBase)
            .statusCode(HttpStatus.SC_UNAUTHORIZED)
            .body("error.status", is(401),
                  "error.reason", is("Access Denied: Not Authenticated"),
            "message", is("authentication error"))
    	// @formatter:on
    }

	@Test
	void 'login with empty credentials'() {
		emptyCredentials()
		// @formatter:off
        given()
            .spec(reqSpecBase)
            .body(loginRequest)
        .when()
            .post(LOGIN_ENDPOINT)
        .then()
            .spec(respSpecBase)
            .statusCode(HttpStatus.SC_BAD_REQUEST)
            .body("error.status", is(400),
                  "error.reason", is("method 'login' parameter 0 'loginRequest': Validation failure"),
                    "message", is("request data conversion error"))
    	// @formatter:on
	}

	@Test
    void 'login with invalid credentials'() {
        invalidCredentials()
        // @formatter:off
        given()
            .spec(reqSpecBase)
            .body(loginRequest)
        .when()
            .post(LOGIN_ENDPOINT)
        .then()
            .spec(respSpecBase)
            .statusCode(HttpStatus.SC_UNAUTHORIZED)
            .body("error.status", is(401),
                  "error.reason", is("Invalid Credentials"),
            "message", is("authentication error"))
    	// @formatter:on
    }

    @Test
    void 'login and acquire jwt'() {
        validCredentials()
        // @formatter:off
        String jwt = given()
                        .spec(reqSpecBase)
                        .body(loginRequest)
                    .when()
                        .post(LOGIN_ENDPOINT)
                    .then()
                        .spec(respSpecBase)
                        .statusCode(HttpStatus.SC_CREATED)
                        .body("token", is(notNullValue(String.class)))
                            .extract().path("token")
    	// @formatter:on
        println "jwt = $jwt"
        // verify jwt
	    def jwtProperties = appProperties.getJwt()
	    def jwtParser = Jwts.parser()
			    .setSigningKey(jwtProperties.getSecret().getBytes())
//                          .setAllowedClockSkewSeconds(5L)
        jwtProperties.getAudience().ifPresent({ a -> jwtParser.requireAudience(a) })
	    def jwtBody = jwtParser.parseClaimsJws(jwt).getBody()
        println "jwt claims: $jwtBody"
	    def jwtId = UUID.fromString(jwtBody.getId())
        assertThat(jwtId, is(notNullValue(UUID.class)))
        assertThat(jwtBody.getSubject(), is("testuser"))
	    assertThat(jwtBody.getIssuer(), is("http://localhost".toString()))
	    assertThat(jwtBody.get("login", String.class), is(equalTo(jwtBody.getSubject())))
        Collection<String> authorities = jwtBody.get("authorities", Collection.class)
	    assertThat(authorities, contains("ROLE_USER") as Matcher<? super Collection>)
    }
}
