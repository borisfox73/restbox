/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.integrationTests

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
import org.springframework.test.context.junit4.SpringRunner

import static io.restassured.RestAssured.given
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath
import static org.hamcrest.Matchers.*

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@TestPropertySource(properties = [
//        "app.config.users[0].username=testuser",
//        "app.config.users[0].password=testpass",
//        "app.config.users[0].roles=USER,ADMIN",
//        "app.config.jwt.time-to-live=PT1M"
//])
@ActiveProfiles("test")
class IT30WebSpaController {

//	@ContextConfiguration
//	public class SpringTestConfig {	}

	private static final String LOGIN_ENDPOINT = "/login"
	private static final String GETCONFIG_ENDPOINT = "/webapi/list/boxcontrollers"
	private static final String GETLIGHTS_ENDPOINT = "/webapi/get/{boxName}/{boxControlType}/{boxControlId}"
	private static final String PUTLIGHTS_ENDPOINT = "/webapi/change/{boxName}/{boxControlType}/{boxControlId}/{status}"

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
	private String jwt


	@Before
	void initRestAssured() {
		RestAssured.port = serverPort
		RestAssured.filters(new ResponseLoggingFilter())
		RestAssured.filters(new RequestLoggingFilter())
		jwt = acquireJwt("testuser", "testpass")
	}

	private static String acquireJwt(String username, String password) {
		LoginRequest loginRequest = new LoginRequest(username, password)
		// @formatter:off
        return given()
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
	}

	@Test
	void 'unauthenticated'() {
		// @formatter:off
        given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .when()
                .get(GETCONFIG_ENDPOINT)
            .then()
		        .assertThat()
	                .statusCode(HttpStatus.SC_UNAUTHORIZED)
                    .body('message', is('authentication error'),
			        'error.path', is('/webapi/list/boxcontrollers'),
		            'error.status', is(401),
		            'error.error', is('Unauthorized'),
		            'error.reason', is('Access Denied: Not Authenticated') )
    	// @formatter:on
	}

	@Test
	void 'get config'() {
/*
	    JsonSchemaFactory jsonSchemaFactory = JsonSchemaFactory.newBuilder()
			    .setValidationConfiguration(
			    ValidationConfiguration.newBuilder()
					    .setDefaultVersion(SchemaVersion.DRAFTV4).freeze())
			    .freeze()
*/

		// @formatter:off
        given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .auth().preemptive()
                .oauth2(jwt)
            .when()
                .get(GETCONFIG_ENDPOINT)
            .then()
		        .assertThat()
                    .statusCode(HttpStatus.SC_OK)
                    .body(matchesJsonSchemaInClasspath("config_schema.json")
                            /*.using(jsonSchemaFactory)*/ )
    	// @formatter:on
	}

	@Test
	void 'get lights b1 led 0'() {
		getLights('b1', 'led', 0, 0)
	}

	@Test
	void 'get lights b2 led 0'() {
		getLights('b2', 'led', 0, 0)
	}

	@Test
	void 'get lights b2 led 1'() {
		getLights('b2', 'led', 1, 0)
	}

	@Test
	void 'put lights b1 led 0'() {
		putLights('b1', 'led', 0, 0)
	}

	@Test
	void 'put and get lights b1 led 1'() {
		putLights('b1', 'led', 1, 1)
		getLights('b1', 'led', 1, 1)
		putLights('b1', 'led', 1, 0)
		getLights('b1', 'led', 1, 0)
	}

	@Test
	void 'box not found'() {
		def endpoint = GETLIGHTS_ENDPOINT.replace('{boxName}', 'nonexistentbox').replace('{boxControlType}', 'led').replace('{boxControlId}', "1")
		print "GET endpoint = $endpoint"
		// @formatter:off
        given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .auth().preemptive()
                .oauth2(jwt)
            .when()
                .get(endpoint)
            .then()
                .assertThat()
                    .statusCode(HttpStatus.SC_NOT_FOUND)
                    .body("message", is("Box 'nonexistentbox' not found"))
    	// @formatter:on
	}

	@Test
	void 'control not found (get)'() {
		def endpoint = GETLIGHTS_ENDPOINT.replace('{boxName}', 'b1').replace('{boxControlType}', 'led').replace('{boxControlId}', "5")
		print "GET endpoint = $endpoint"
		// @formatter:off
        given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .auth().preemptive()
                .oauth2(jwt)
            .when()
                .get(endpoint)
            .then()
                .assertThat()
                    .statusCode(HttpStatus.SC_NOT_FOUND)
                    .body("message", is("Control with type 'LED' and id 5 not found"))
    	// @formatter:on
	}

	@Test
	void 'control not found (put)'() {
		def endpoint = PUTLIGHTS_ENDPOINT.replace('{boxName}', 'b1').replace('{boxControlType}', 'led').replace('{boxControlId}', "5").replace('{status}', "1")
		print "PUT endpoint = $endpoint"
		// @formatter:off
        given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .auth().preemptive()
                .oauth2(jwt)
            .when()
                .put(endpoint)
            .then()
                .assertThat()
                    .statusCode(HttpStatus.SC_NOT_FOUND)
                    .body("message", is("Control with type 'LED' and id 5 not found"))
    	// @formatter:on
	}

	private void getLights(final String boxName, final String boxControlType, final int boxControlId, final int expected) {
		def endpoint = GETLIGHTS_ENDPOINT.replace('{boxName}', boxName).replace('{boxControlType}', boxControlType).replace('{boxControlId}', boxControlId.toString())
		print "GET endpoint = $endpoint"
		// @formatter:off
        given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .auth().preemptive()
                .oauth2(jwt)
            .when()
                .get(endpoint)
            .then()
                .assertThat()
                    .statusCode(HttpStatus.SC_OK)
                    .body("message", is(equalTo(expected)))
    	// @formatter:on
	}

	private void putLights(final String boxName, final String boxControlType, final int boxControlId, final int status) {
		def endpoint = PUTLIGHTS_ENDPOINT.replace('{boxName}', boxName).replace('{boxControlType}', boxControlType).replace('{boxControlId}', boxControlId.toString()).replace('{status}', status.toString())
		print "PUT endpoint = $endpoint"
		// @formatter:off
        given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .auth().preemptive()
                .oauth2(jwt)
            .when()
                .put(endpoint)
            .then()
                .assertThat()
                    .statusCode(HttpStatus.SC_OK)
                    .body("message", is('ok'))
    	// @formatter:on
	}
}
