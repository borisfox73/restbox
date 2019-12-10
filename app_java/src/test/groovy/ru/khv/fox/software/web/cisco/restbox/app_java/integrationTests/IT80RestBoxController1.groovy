/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.integrationTests

import io.restassured.RestAssured
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
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.is

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class IT80RestBoxController1 {

	private static final String BOXAPI_ENDPOINT_BASE = "/api"
	private static final String GETSTATUS_ENDPOINT = "/get/{boxName}/{secret}/{boxControlType}/{boxControlId}"
	private static final String PUTSTATUS_ENDPOINT = "/put/{boxName}/{secret}/{boxControlType}/{boxControlId}/{status}"

	@LocalServerPort
	private int serverPort

	private static RequestSpecification reqSpecBase
	private static ResponseSpecification respSpecBase


	@Before
	void init() {
		initRestAssured(serverPort)
	}

	static void initRestAssured(final int serverPort) {
		if (reqSpecBase == null) {
			RestAssured.port = serverPort
			RestAssured.enableLoggingOfRequestAndResponseIfValidationFails()
			reqSpecBase = new RequestSpecBuilder()
					.setContentType(ContentType.JSON)
					.setAccept(ContentType.JSON)
					.setBasePath(BOXAPI_ENDPOINT_BASE)
					.log(LogDetail.ALL)
					.build()
			respSpecBase = new ResponseSpecBuilder()
					.expectContentType(ContentType.JSON)
					.build()
		}
	}

	// TODO mock Cisco API service to test actions without the live router

    @Test
    void 'put status b1 switch 0 = 0'() {
        putStatus('b1', 'cisco123', 'switch', 0, 0)
    }

    @Test
    void 'put status b1 switch 0 = 1'() {
        putStatus('b1', 'cisco123', 'switch', 0, 1)
    }

    @Test
    void 'put status b1 switch 1 = 0'() {
        putStatus('b1', 'cisco123', 'switch', 1, 0)
    }

    @Test
    void 'put status b1 switch 1 = 1'() {
        putStatus('b1', 'cisco123', 'switch', 1, 1)
	}

	@Test
	void 'put status b1 button 0 = 0'() {
		putStatus('b1', 'cisco123', 'button', 0, 0)
	}

	@Test
	void 'put status b1 button 0 = 1'() {
		putStatus('b1', 'cisco123', 'button', 0, 1)
	}

    @Test
    void 'put status b1 usonic 0 = 20'() {
        putStatus('b1', 'cisco123', 'usonic', 0, 20)
    }

    @Test
    void 'put status b1 usonic 0 = 80'() {
        putStatus('b1', 'cisco123', 'usonic', 0, 80)
    }

    @Test
    void 'put status b2 switch 0 = 0'() {
        putStatus('b2', 'cisco456', 'switch', 0, 0)
    }

    @Test
    void 'put status b2 switch 0 = 1'() {
        putStatus('b2', 'cisco456', 'switch', 0, 1)
    }

    @Test
    void 'put status b2 button 0 = 0'() {
        putStatus('b2', 'cisco456', 'button', 0, 0)
    }

    @Test
    void 'put status b2 button 0 = 1'() {
        putStatus('b2', 'cisco456', 'button', 0, 1)
    }

    @Test
	void 'put status b2 usonic 0 = 0'() {
		putStatus('b2', 'cisco456', 'usonic', 0, 0)
	}

	@Test
	void 'put status b2 usonic 0 = 10'() {
		putStatus('b2', 'cisco456', 'usonic', 0, 10)
	}

	@Test
	void 'put status b2 usonic 0 = 70'() {
		putStatus('b2', 'cisco456', 'usonic', 0, 70)
	}

	@Test
	void 'put status b2 usonic 0 = 120'() {
		putStatus('b2', 'cisco456', 'usonic', 0, 120)
	}

	@Test
	void 'put and get status b1 button 0'() {
		putStatus('b1', 'cisco123', 'button', 0, 1)
		getStatus('b1', 'cisco123', 'button', 0, 1)
		putStatus('b1', 'cisco123', 'button', 0, 0)
		getStatus('b1', 'cisco123', 'button', 0, 0)
	}

	@Test
	void 'box not found'() {
		def pathParams = Map.of('boxName', 'nonexistentbox',
				'secret', 'cisco123',
				'boxControlType', 'led',
				'boxControlId', '1')
		callGetEndpoint(GETSTATUS_ENDPOINT, pathParams, Collections.emptyMap(), "Box 'nonexistentbox' not found", HttpStatus.SC_NOT_FOUND)
	}

	@Test
	void 'control not found (get)'() {
		def pathParams = Map.of('boxName', 'b1',
				'secret', 'cisco123',
				'boxControlType', 'led',
				'boxControlId', '5')
		callGetEndpoint(GETSTATUS_ENDPOINT, pathParams, Collections.emptyMap(), 'Control with type \'LED\' and id 5 not found', HttpStatus.SC_NOT_FOUND)
	}

	@Test
	void 'control not found (put)'() {
		def pathParams = Map.of('boxName', 'b1',
				'secret', 'cisco123',
				'boxControlType', 'led',
				'boxControlId', '5',
				'status', '1')
		callPutEndpoint(PUTSTATUS_ENDPOINT, pathParams, 'Control with type \'LED\' and id 5 not found', HttpStatus.SC_NOT_FOUND)
	}

	@Test
	void 'authentication error'() {
		def pathParams = Map.of('boxName', 'b1',
				'secret', 'wrong',
				'boxControlType', 'led',
				'boxControlId', '5')
		callGetEndpoint(GETSTATUS_ENDPOINT, pathParams, Collections.emptyMap(), 'auth_error', HttpStatus.SC_UNAUTHORIZED)
	}


	static void getStatus(final String boxName, final String secret, final String boxControlType, final int boxControlId, final int expected, final boolean inline = false) {
		def pathParams = Map.of('boxName', boxName,
				'secret', secret,
				'boxControlType', boxControlType,
				'boxControlId', boxControlId.toString())
		def queryParams = Map.of('inline', inline)
		callGetEndpoint(GETSTATUS_ENDPOINT, pathParams, queryParams, expected)
	}

	static void putStatus(final String boxName, final String secret, final String boxControlType, final int boxControlId, final int status) {
		def pathParams = Map.of('boxName', boxName,
				'secret', secret,
				'boxControlType', boxControlType,
				'boxControlId', boxControlId,
				'status', status)
		callPutEndpoint(PUTSTATUS_ENDPOINT, pathParams)
	}

	static void callGetEndpoint(final String endpoint, final Map<String, Object> pathParams = Collections.emptyMap(), final Map<String, Object> queryParams = Collections.emptyMap(), final Object expected = 'ok', final int expectedStatus = HttpStatus.SC_OK) {
		println "GET endpoint = $endpoint"
		// @formatter:off
        given()
            .spec(reqSpecBase)
            .pathParams(pathParams)
            .queryParams(queryParams)
            .when()
                .get(endpoint)
            .then()
                .assertThat()
                    .spec(respSpecBase)
                    .statusCode(expectedStatus)
                    .body("message", is(equalTo(expected)))
    	// @formatter:on
	}

	static void callPutEndpoint(final String endpoint, final Map<String, Object> pathParams = Collections.emptyMap(), final Object expected = 'ok', final int expectedStatus = HttpStatus.SC_OK) {
		println "GET endpoint = $endpoint"
		// @formatter:off
        given()
            .spec(reqSpecBase)
            .when()
                .put(endpoint, pathParams)
            .then()
                .assertThat()
                    .spec(respSpecBase)
                    .statusCode(expectedStatus)
                    .body("message", is(equalTo(expected)))
    	// @formatter:on
	}
}
