/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.integrationTests

import com.fasterxml.jackson.annotation.JsonProperty
import io.restassured.RestAssured
import io.restassured.authentication.PreemptiveOAuth2HeaderScheme
import io.restassured.builder.RequestSpecBuilder
import io.restassured.builder.ResponseSpecBuilder
import io.restassured.filter.log.LogDetail
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import io.restassured.specification.ResponseSpecification
import org.apache.http.HttpStatus
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath
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
@DirtiesContext
class IT90WebSpaController {
	private static final String LOGIN_ENDPOINT = "/login"
	private static final String WEBAPI_ENDPOINT_BASE = "/webapi"
	private static final String LISTBOXCONTROLLERS_ENDPOINT = "/list/boxcontrollers"
	private static final String LISTLABUSERS_ENDPOINT = "/list/labusers"
	private static final String LISTROUTERS_ENDPOINT = "/list/routers"
	private static final String LISTAFUNCTIONS_ENDPOINT = "/list/afunctions"
	private static final String LISTRFUNCTIONS_ENDPOINT = "/list/rfunctions"
	private static final String GETSTATUS_ENDPOINT = "/get/{boxName}/{boxControlType}/{boxControlId}"
	private static final String GETACTION_ENDPOINT = "/getaction/{boxName}/{boxControlType}/{boxControlId}"
	private static final String PUTSTATUS_ENDPOINT = "/change/{boxName}/{boxControlType}/{boxControlId}/{status}"
	private static final String ONFUNC_ENDPOINT = "/onfunc/{boxName}/{boxControlType}/{boxControlId}/{func}"
	private static final String OFFFUNC_ENDPOINT = "/offfunc/{boxName}/{boxControlType}/{boxControlId}/{func}"
	private static final String RFUNC_ENDPOINT = "/rfunc/{boxName}/{boxControlType}/{boxControlId}/{func}"
	private static final String CALL_AFUNC_ENDPOINT = "/call/afunc/{func}"
	private static final String CALL_RFUNC_ENDPOINT = "/call/rfunc/{func}"
	private static final String REAUTH_ENDPOINT = "/csr/reauth"

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
	private static RequestSpecification reqSpecBase, reqSpec
	private static ResponseSpecification respSpecBase, respSpecApiSerialized


	@Before
	void initRestAssured() {
		if (reqSpecBase == null) {
			RestAssured.port = serverPort
//		    RestAssured.filters(new ResponseLoggingFilter())
//		    RestAssured.filters(new RequestLoggingFilter())
			RestAssured.enableLoggingOfRequestAndResponseIfValidationFails()
			reqSpecBase = new RequestSpecBuilder()
					.setContentType(ContentType.JSON)
					.setAccept(ContentType.JSON)
					.setBasePath(WEBAPI_ENDPOINT_BASE)
					.log(LogDetail.ALL)
					.build()
			respSpecBase = new ResponseSpecBuilder()
					.expectContentType(ContentType.JSON)
					.build()
			def authScheme = new PreemptiveOAuth2HeaderScheme()
			authScheme.accessToken = acquireJwt("testuser", "testpass")
			reqSpec = new RequestSpecBuilder()
					.addRequestSpecification(reqSpecBase)
					.setAuth(authScheme)
					.build()
			respSpecApiSerialized = new ResponseSpecBuilder()
					.addResponseSpecification(respSpecBase)
					.expectStatusCode(HttpStatus.SC_OK)
					.expectBody(matchesJsonSchemaInClasspath("api_response_schema.json"))
					.build()
		}
	}

	private static String acquireJwt(String username, String password) {
		LoginRequest loginRequest = new LoginRequest(username, password)
		// @formatter:off
        return given()
                    .spec(reqSpecBase)
                    .basePath("")
                    .body(loginRequest)
               .when()
                    .post(LOGIN_ENDPOINT)
               .then()
                    .spec(respSpecBase)
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
            .spec(reqSpecBase)
            .when()
                .get(LISTBOXCONTROLLERS_ENDPOINT)
            .then()
		        .assertThat()
                    .spec(respSpecBase)
	                .statusCode(HttpStatus.SC_UNAUTHORIZED)
                    .body('message', is('authentication error'),
			        'error.path', is('/webapi/list/boxcontrollers'),
		            'error.status', is(401),
		            'error.error', is('Unauthorized'),
		            'error.reason', is('Access Denied: Not Authenticated') )
    	// @formatter:on
	}

	@Test
	void 'list boxcontrollers'() {
		// @formatter:off
        given()
            .spec(reqSpec)
	        .when()
                .get(LISTBOXCONTROLLERS_ENDPOINT)
            .then()
		        .assertThat()
                    .spec(respSpecApiSerialized)
					.body('message', allOf(matchesJsonSchemaInClasspath("boxcontrollers_schema.json"),
												 isJson(withJsonPath('$', hasSize(2)))))
    	// @formatter:on
	}

	@Test
	void 'list lab users'() {
		// @formatter:off
        given()
            .spec(reqSpec)
            .when()
                .get(LISTLABUSERS_ENDPOINT)
            .then()
				.assertThat()
                    .spec(respSpecApiSerialized)
					.body('message', allOf(matchesJsonSchemaInClasspath("labusers_schema.json"),
											 isJson(allOf(withJsonPath('$', hasSize(3)),
														  withJsonPath('$..login', containsInAnyOrder("testadmin", "testuser", "test2")),
														  withJsonPath('$..password', containsInAnyOrder("admpass", "testpass", "pass2"))))))
    	// @formatter:on
	}

	@Test
	void 'list routers'() {
		// @formatter:off
        given()
            .spec(reqSpec)
			.when()
				.get(LISTROUTERS_ENDPOINT)
			.then()
				.assertThat()
                    .spec(respSpecApiSerialized)
					.body('message', allOf(matchesJsonSchemaInClasspath("routers_schema.json"),
															 isJson(allOf(withJsonPath('$', hasSize(2)),
																	 withJsonPath('$..name', containsInAnyOrder("CSR-WAN", "CSR-AWS")),
																	 withJsonPath('$..host', containsInAnyOrder("192.168.70.70", "192.168.70.71")),
																	 withJsonPath('$..username', containsInAnyOrder("rest", "rest2")),
																	 withJsonPath('$..password', containsInAnyOrder("restpwd", "restpwd2")),
																	 withJsonPath('$..type', containsInAnyOrder("csrv", "asr"))))))
    	// @formatter:on
	}

	@Test
	void 'list afunctions'() {
		// @formatter:off
        given()
            .spec(reqSpec)
			.when()
				.get(LISTAFUNCTIONS_ENDPOINT)
			.then()
				.assertThat()
                    .spec(respSpecApiSerialized)
					.body('message', allOf(matchesJsonSchemaInClasspath("routerfunctions_schema.json"),
												 isJson(withJsonPath('$', hasSize(13)))))
    	// @formatter:on
	}

	@Test
	void 'list rfunctions'() {
		// @formatter:off
        given()
            .spec(reqSpec)
			.when()
				.get(LISTRFUNCTIONS_ENDPOINT)
			.then()
				.assertThat()
                    .spec(respSpecApiSerialized)
					.body('message', allOf(matchesJsonSchemaInClasspath("routerfunctions_schema.json"),
												 isJson(withJsonPath('$', hasSize(5)))))
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
		testStatus('b1', 'usonic', 0, 20, "ON")
	}

	@Test
	void 'status b1 usonic 0 (80)'() {
		testStatus('b1', 'usonic', 0, 80, "OFF")
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
		testStatus('b2', 'usonic', 0, 10, "ON")
	}

	@Test
	void 'status b2 usonic 0 (70)'() {
		testStatus('b2', 'usonic', 0, 70, "OFF")
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
		def pathParams = Map.of('boxName', 'nonexistentbox',
				'boxControlType', 'led',
				'boxControlId', "1")
		callGetEndpoint(GETSTATUS_ENDPOINT, pathParams, "Box 'nonexistentbox' not found", HttpStatus.SC_NOT_FOUND)
	}

	@Test
	void 'control not found (get)'() {
		def pathParams = Map.of('boxName', 'b1',
				'boxControlType', 'led',
				'boxControlId', "5")
		callGetEndpoint(GETSTATUS_ENDPOINT, pathParams, "Control with type 'LED' and id 5 not found", HttpStatus.SC_NOT_FOUND)
	}

	@Test
	void 'control not found (put)'() {
		def pathParams = Map.of('boxName', 'b1',
				'boxControlType', 'led',
				'boxControlId', "5",
				'status', "1")
		callPutEndpoint(PUTSTATUS_ENDPOINT, pathParams, "Control with type 'LED' and id 5 not found", HttpStatus.SC_NOT_FOUND)
	}

	private static void testStatus(final String boxName, final String boxControlType, final int boxControlId, final int highStatus = 1, final String expected = null) {
		putStatus(boxName, boxControlType, boxControlId, 0)
		getStatus(boxName, boxControlType, boxControlId, 0)
		putStatus(boxName, boxControlType, boxControlId, highStatus)
		getStatus(boxName, boxControlType, boxControlId, highStatus)
		if (expected != null)
			getAction(boxName, boxControlType, boxControlId, expected)
	}

	private static void getStatus(final String boxName, final String boxControlType, final int boxControlId, final int expected) {
		def pathParams = Map.of('boxName', boxName,
				'boxControlType', boxControlType,
				'boxControlId', boxControlId)
		callGetEndpoint(GETSTATUS_ENDPOINT, pathParams, expected)
	}

	private static void getAction(final String boxName, final String boxControlType, final int boxControlId, final String expected) {
		def pathParams = Map.of('boxName', boxName,
				'boxControlType', boxControlType,
				'boxControlId', boxControlId)
		callGetEndpoint(GETACTION_ENDPOINT, pathParams, expected)
	}

	private static void putStatus(final String boxName, final String boxControlType, final int boxControlId, final int status) {
		def pathParams = Map.of('boxName', boxName,
				'boxControlType', boxControlType,
				'boxControlId', boxControlId,
				'status', status)
		callPutEndpoint(PUTSTATUS_ENDPOINT, pathParams)
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

	private static void putFunc(final String endpointTemplate, final String boxName, final String boxControlType, final int boxControlId, final String func, final int status = HttpStatus.SC_OK, final String message = 'ok') {
		def pathParams = Map.of('func', func,
				'boxName', boxName,
				'boxControlType', boxControlType,
				'boxControlId', boxControlId)
		callPutEndpoint(endpointTemplate, pathParams, message, status)
	}

	private static void callFunc(final String endpointTemplate, final String func, final int status = HttpStatus.SC_OK, final String message = 'ok') {
		def pathParams = Map.of('func', func)
		callPutEndpoint(endpointTemplate, pathParams, message, status)
	}

	private static void callGetEndpoint(final String endpoint, final Map<String, Object> pathParams = Collections.emptyMap(), final Object expected = 'ok', final int status = HttpStatus.SC_OK) {
		print "GET endpoint = $endpoint"
		// @formatter:off
        given()
            .spec(reqSpec)
            .when()
                .get(endpoint, pathParams)
            .then()
                .assertThat()
                    .spec(respSpecBase)
                    .statusCode(status)
                    .body("message", equalTo(expected))
    	// @formatter:on
	}

	private static void callPutEndpoint(final String endpoint, final Map<String, Object> pathParams = Collections.emptyMap(), final String message = 'ok', final int status = HttpStatus.SC_OK) {
		print "PUT endpoint = $endpoint"
		// @formatter:off
        given()
            .spec(reqSpec)
            .when()
                .put(endpoint, pathParams)
            .then()
                .assertThat()
                    .spec(respSpecBase)
                    .statusCode(status)
                    .body("message", equalTo(message))
    	// @formatter:on
	}
}
