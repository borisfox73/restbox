/*
 * Copyright (c) 2019 Boris Fox.
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
import static org.hamcrest.MatcherAssert.assertThat
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
class IT90WebSpaController {

//	@ContextConfiguration
//	public class SpringTestConfig {	}

	private static final String LOGIN_ENDPOINT = "/login"
	private static final String GETCONFIG_ENDPOINT = "/webapi/list/boxcontrollers"
	private static final String LISTLABUSERS_ENDPOINT = "/webapi/list/labusers"
	private static final String LISTROUTERS_ENDPOINT = "/webapi/list/routers"
	private static final String LISTAFUNCTIONS_ENDPOINT = "/webapi/list/afunctions"
	private static final String LISTRFUNCTIONS_ENDPOINT = "/webapi/list/rfunctions"
	private static final String GETSTATUS_ENDPOINT = "/webapi/get/{boxName}/{boxControlType}/{boxControlId}"
	private static final String GETACTION_ENDPOINT = "/webapi/getaction/{boxName}/{boxControlType}/{boxControlId}"
	private static final String PUTSTATUS_ENDPOINT = "/webapi/change/{boxName}/{boxControlType}/{boxControlId}/{status}"
	private static final String ONFUNC_ENDPOINT = "/webapi/onfunc/{boxName}/{boxControlType}/{boxControlId}/{func}"
	private static final String OFFFUNC_ENDPOINT = "/webapi/offfunc/{boxName}/{boxControlType}/{boxControlId}/{func}"
	private static final String RFUNC_ENDPOINT = "/webapi/rfunc/{boxName}/{boxControlType}/{boxControlId}/{func}"
	private static final String CALL_AFUNC_ENDPOINT = "/webapi/call/afunc/{func}"
	private static final String CALL_RFUNC_ENDPOINT = "/webapi/call/rfunc/{func}"
	private static final String REAUTH_ENDPOINT = "/webapi/csr/reauth"

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
		// TODO split in two and set state of external resources to test indicator endpoints
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
	void 'list lab users'() {
		// @formatter:off
        def userList = given()
						.contentType(ContentType.JSON)
			            .accept(ContentType.JSON)
			            .auth().preemptive()
			                .oauth2(jwt)
			            .when()
			                .get(LISTLABUSERS_ENDPOINT)
			            .then()
					        .assertThat()
			                    .statusCode(HttpStatus.SC_OK)
					            .contentType(ContentType.JSON)
			                    .body(matchesJsonSchemaInClasspath("labusers_schema.json"))
					            .body("message.size()", is(3))
			                    .body("message.login", containsInAnyOrder("testadmin", "testuser", "test2"))
			                    .body("message.password", containsInAnyOrder("admpass", "testpass", "pass2"))
					        .and()
			                    .extract()
			                        .response()
			                            .jsonPath()
			                                .getList("message")
		//noinspection GroovyAssignabilityCheck
		assertThat(userList, containsInAnyOrder(
				allOf(hasEntry("login", "testadmin"), hasEntry("password", "admpass")),
				allOf(hasEntry("login", "testuser"), hasEntry("password", "testpass")),
				allOf(hasEntry("login", "test2"), hasEntry("password", "pass2"))
		))
    	// @formatter:on
	}

	@Test
	void 'list routers'() {
		// @formatter:off
        given()
			.contentType(ContentType.JSON)
		    .accept(ContentType.JSON)
		    .auth().preemptive()
		        .oauth2(jwt)
			.when()
				.get(LISTROUTERS_ENDPOINT)
			.then()
				.assertThat()
			        .statusCode(HttpStatus.SC_OK)
					.contentType(ContentType.JSON)
			        .body(matchesJsonSchemaInClasspath("routers_schema.json"))
					.body("message.size()", is(2))
			        .body("message.name", containsInAnyOrder("CSR-WAN", "CSR-AWS"))
			        .body("message.host", containsInAnyOrder("192.168.70.70", "192.168.70.71"))
			        .body("message.username", containsInAnyOrder("rest", "rest2"))
			        .body("message.password", containsInAnyOrder("restpwd", "restpwd2"))
			        .body("message.type", containsInAnyOrder("csrv", "asr"))
    	// @formatter:on
	}

	@Test
	void 'list afunctions'() {
		// @formatter:off
        given()
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.auth().preemptive()
				.oauth2(jwt)
			.when()
				.get(LISTAFUNCTIONS_ENDPOINT)
			.then()
				.assertThat()
					.statusCode(HttpStatus.SC_OK)
					.contentType(ContentType.JSON)
			        .body(matchesJsonSchemaInClasspath("routerfunctions_schema.json"))
					.body("message.size()", is(13))
    	// @formatter:on
	}

	@Test
	void 'list rfunctions'() {
		// @formatter:off
        given()
			.contentType(ContentType.JSON)
			.accept(ContentType.JSON)
			.auth().preemptive()
				.oauth2(jwt)
			.when()
				.get(LISTRFUNCTIONS_ENDPOINT)
			.then()
				.assertThat()
			        .statusCode(HttpStatus.SC_OK)
					.contentType(ContentType.JSON)
			        .body(matchesJsonSchemaInClasspath("routerfunctions_schema.json"))
					.body("message.size()", is(5))
    	// @formatter:on
	}

	@Test
	void 'status b1 switch 0'() {
		testStatus('b1', 'switch', 0)
	}

	@Test
	void 'status b1 switch 1'() {
		testStatus('b1', 'switch', 1)
	}

	@Test
	void 'status b1 button 0'() {
		testStatus('b1', 'button', 0)
	}

	@Test
	void 'status b1 usonic 0 (20)'() {
		testStatus('b1', 'usonic', 0, 20, "OFF")
	}

	@Test
	void 'status b1 usonic 0 (80)'() {
		testStatus('b1', 'usonic', 0, 80, "ON")
	}

	@Test
	void 'status b2 switch 0'() {
		testStatus('b2', 'switch', 0)
	}

	@Test
	void 'status b2 button 0'() {
		testStatus('b2', 'button', 0)
	}

	@Test
	void 'status b2 usonic 0 (0)'() {
		testStatus('b2', 'usonic', 0, 0, "NOOP")
	}

	@Test
	void 'status b2 usonic 0 (10)'() {
		testStatus('b2', 'usonic', 0, 10, "OFF")
	}

	@Test
	void 'status b2 usonic 0 (70)'() {
		testStatus('b2', 'usonic', 0, 70, "ON")
	}

	@Test
	void 'status b2 usonic 0 (120)'() {
		testStatus('b2', 'usonic', 0, 120, "NOOP")
	}


	@Test
	void 'status b1 led 0'() {
		testStatus('b1', 'led', 0)
	}

	@Test
	void 'status b1 led 1'() {
		testStatus('b1', 'led', 1)
	}

	@Test
	void 'status b2 led 0'() {
		testStatus('b2', 'led', 0)
	}

	@Test
	void 'status b2 led 1'() {
		testStatus('b2', 'led', 1)
	}


	@Test
	void 'box not found'() {
		def endpoint = GETSTATUS_ENDPOINT.replace('{boxName}', 'nonexistentbox').replace('{boxControlType}', 'led').replace('{boxControlId}', "1")
		callGetEndpoint(endpoint, HttpStatus.SC_NOT_FOUND, "Box 'nonexistentbox' not found")
	}

	@Test
	void 'control not found (get)'() {
		def endpoint = GETSTATUS_ENDPOINT.replace('{boxName}', 'b1').replace('{boxControlType}', 'led').replace('{boxControlId}', "5")
		callGetEndpoint(endpoint, HttpStatus.SC_NOT_FOUND, "Control with type 'LED' and id 5 not found")
	}

	@Test
	void 'control not found (put)'() {
		def endpoint = PUTSTATUS_ENDPOINT.replace('{boxName}', 'b1').replace('{boxControlType}', 'led').replace('{boxControlId}', "5").replace('{status}', "1")
		callPutEndpoint(endpoint, HttpStatus.SC_NOT_FOUND, "Control with type 'LED' and id 5 not found")
    	// @formatter:on
	}

	private void testStatus(final String boxName, final String boxControlType, final int boxControlId, final int highStatus = 1, final String expected = null) {
		putStatus(boxName, boxControlType, boxControlId, 0)
		getStatus(boxName, boxControlType, boxControlId, 0)
		putStatus(boxName, boxControlType, boxControlId, highStatus)
		getStatus(boxName, boxControlType, boxControlId, highStatus)
		if (expected != null)
			getAction(boxName, boxControlType, boxControlId, expected)
	}

	private void getStatus(final String boxName, final String boxControlType, final int boxControlId, final int expected) {
		def endpoint = GETSTATUS_ENDPOINT.replace('{boxName}', boxName).replace('{boxControlType}', boxControlType).replace('{boxControlId}', boxControlId.toString())
		callGetEndpointInt(endpoint, expected)
	}

	private void getAction(final String boxName, final String boxControlType, final int boxControlId, final String expected) {
		def endpoint = GETACTION_ENDPOINT.replace('{boxName}', boxName).replace('{boxControlType}', boxControlType).replace('{boxControlId}', boxControlId.toString())
		callGetEndpoint(endpoint, HttpStatus.SC_OK, expected)
	}

	private void putStatus(final String boxName, final String boxControlType, final int boxControlId, final int status) {
		def endpoint = PUTSTATUS_ENDPOINT.replace('{boxName}', boxName).replace('{boxControlType}', boxControlType).replace('{boxControlId}', boxControlId.toString()).replace('{status}', status.toString())
		callPutEndpoint(endpoint)
	}

	@Test
	void 'put onfunc anone'() {
		putFunc(ONFUNC_ENDPOINT, "b1", "switch", 0, "anone")
	}

	@Test
	void 'put offunc anone'() {
		putFunc(OFFFUNC_ENDPOINT, "b1", "switch", 0, "anone")
	}

	@Test
	void 'put rfunc rnone'() {
		putFunc(RFUNC_ENDPOINT, "b1", "led", 0, "rnone")
	}

	@Test
	void 'put onfunc afunc1'() {
		putFunc(ONFUNC_ENDPOINT, "b2", "usonic", 0, "afunc1")
	}

	@Test
	void 'put offunc afunc2'() {
		putFunc(OFFFUNC_ENDPOINT, "b2", "usonic", 0, "afunc1")
	}

	@Test
	void 'put rfunc rfunc1'() {
		putFunc(RFUNC_ENDPOINT, "b2", "led", 1, "rfunc1")
	}

	// Test errors (func not found / wrong type)
	@Test
	void 'put onfunc afunc000'() {
		putFunc(ONFUNC_ENDPOINT, "b2", "usonic", 0, "afunc000", HttpStatus.SC_NOT_FOUND, "Function 'afunc000' is not found")
	}

	@Test
	void 'put offunc rfunc1'() {
		putFunc(OFFFUNC_ENDPOINT, "b2", "usonic", 0, "rfunc1", HttpStatus.SC_BAD_REQUEST, "Function 'rfunc1' is not of Action type")
	}

	@Test
	void 'put rfunc afunc2'() {
		putFunc(RFUNC_ENDPOINT, "b2", "led", 1, "afunc2", HttpStatus.SC_BAD_REQUEST, "Function 'afunc2' is not of Read type")
	}

	@Test
	void 'put rfunc validation err 1'() {
		putFunc(RFUNC_ENDPOINT, " ", "led", 1, "rnone", HttpStatus.SC_BAD_REQUEST, "request constraint violation error")
	}

	@Test
	void 'put rfunc validation err 2'() {
		putFunc(RFUNC_ENDPOINT, "b1", "xxx", 1, "rnone", HttpStatus.SC_BAD_REQUEST, "request data conversion error")
	}


	@Test
	void 'call afunc anone'() {
		callFunc(CALL_AFUNC_ENDPOINT, "anone")
	}

	@Test
	void 'call rfunc rnone'() {
		callFunc(CALL_RFUNC_ENDPOINT, "rnone")
	}

	@Test
	void 'call afunc afunc1-12'() {
		(1..12).each { callFunc(CALL_AFUNC_ENDPOINT, "afunc" + it) }
	}

	@Test
	void 'call rfunc rfunc1-4'() {
		(1..4).each { callFunc(CALL_RFUNC_ENDPOINT, "rfunc" + it) }
	}

	@Test
	void 'call afunc afunc000'() {
		callFunc(CALL_AFUNC_ENDPOINT, "afunc000", HttpStatus.SC_NOT_FOUND, "Function 'afunc000' is not found")
	}

	@Test
	void 'call afunc rfunc1'() {
		callFunc(CALL_AFUNC_ENDPOINT, "rfunc1", HttpStatus.SC_BAD_REQUEST, "Function 'rfunc1' is not of Action type")
	}

	@Test
	void 'call rfunc afunc2'() {
		callFunc(CALL_RFUNC_ENDPOINT, "afunc2", HttpStatus.SC_BAD_REQUEST, "Function 'afunc2' is not of Read type")
	}

	@Test
	void 'csr reauth'() {
		callPutEndpoint(REAUTH_ENDPOINT)
	}

	private void putFunc(final String endpointTemplate, final String boxName, final String boxControlType, final int boxControlId, final String func, final int status = HttpStatus.SC_OK, final String message = 'ok') {
		def endpoint = endpointTemplate.replace('{boxName}', boxName).replace('{boxControlType}', boxControlType).replace('{boxControlId}', boxControlId.toString())
		callFunc(endpoint, func, status, message)
	}

	private void callFunc(final String endpointTemplate, final String func, final int status = HttpStatus.SC_OK, final String message = 'ok') {
		def endpoint = endpointTemplate.replace('{func}', func)
		callPutEndpoint(endpoint, status, message)
	}

	private void callGetEndpointInt(final String endpoint, final int expectedValue) {
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
                    .body("message", equalTo(expectedValue))
    	// @formatter:on
	}

	private void callGetEndpoint(final String endpoint, final int status = HttpStatus.SC_OK, final String message = 'ok') {
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
                    .statusCode(status)
                    .body("message", equalTo(message))
    	// @formatter:on
	}

	private void callPutEndpoint(final String endpoint, final int status = HttpStatus.SC_OK, final String message = 'ok') {
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
                    .statusCode(status)
                    .body("message", equalTo(message))
    	// @formatter:on
	}
}
