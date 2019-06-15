/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.integrationTests

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
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.is

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class IT80RestBoxController2 {

//	@ContextConfiguration
//	public class SpringTestConfig {	}

    private static final String GETSTATUS_ENDPOINT = "/api/get/{boxName}/{secret}/{boxControlType}/{boxControlId}"
    private static final String PUTSTATUS_ENDPOINT = "/api/put/{boxName}/{secret}/{boxControlType}/{boxControlId}/{status}"

    @LocalServerPort
    private int serverPort

    private static boolean initdone


    @Before
    void initRestAssured() {
        RestAssured.port = serverPort
        RestAssured.filters(new ResponseLoggingFilter())
        RestAssured.filters(new RequestLoggingFilter())
        if (!initdone) {
            // Set interface state for b1 led 0 rfunction
            putStatus('b1', 'cisco123', 'switch', 1, 0)
            // Set acl state for b2 led 1
            putStatus('b2', 'cisco456', 'button', 0, 1)
            Thread.sleep(10 * 1000L) // let interface status settle
            initdone = true
        }
    }

/*
	@AfterClass
	static void reserRouterState() {
		// afunc2
		putStatus('b1', 'cisco123', 'switch', 1, 0)
		// afunc4
		putStatus('b1', 'cisco123', 'button', 0, 0)
		// afunc6
		putStatus('b1', 'cisco123', 'usonic', 0, 0)
		// afunc8
		putStatus('b2', 'cisco456', 'switch', 0, 0)
		// afunc10
		putStatus('b2', 'cisco456', 'button', 0, 0)
		// afunc12
		putStatus('b2', 'cisco456', 'usonic', 0, 0)
		println "Router state has been reset"
	}
*/

    // TODO mock cisco api service to test actions

    @Test
    void 'get status b1 button 0'() {
        getStatus('b1', 'cisco123', 'button', 0, 0)
    }

    @Test
    void 'get status b1 led 0'() {
        // rfunc1
        // inline
        getStatus('b1', 'cisco123', 'led', 0, 0, true)
        // stored
        getStatus('b1', 'cisco123', 'led', 0, 0)
    }

    @Test
    void 'get status b1 led 1'() {
        // rfunc2
        // inline
        getStatus('b1', 'cisco123', 'led', 1, 1, true)
        // stored
        getStatus('b1', 'cisco123', 'led', 1, 1)
    }

    @Test
    void 'get status b2 led 0'() {
        // rfunc3
        // inline
        getStatus('b2', 'cisco456', 'led', 0, 1, true)
        // stored
        getStatus('b2', 'cisco456', 'led', 0, 1)
    }

    @Test
    void 'get status b2 led 1'() {
        // rfunc4
        // inline
        getStatus('b2', 'cisco456', 'led', 1, 1, true)
        // stored
        getStatus('b2', 'cisco456', 'led', 1, 1)
    }

    private static void getStatus(final String boxName, final String secret, final String boxControlType, final int boxControlId, final int expected, final boolean inline = false) {
        def endpoint = GETSTATUS_ENDPOINT.replace('{boxName}', boxName).replace('{secret}', secret).replace('{boxControlType}', boxControlType).replace('{boxControlId}', boxControlId.toString()) + (inline ? "?inline=true" : "")
        println "GET endpoint = $endpoint"
        // @formatter:off
        given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .when()
                .get(endpoint)
            .then()
                .assertThat()
                    .statusCode(HttpStatus.SC_OK)
                    .body("message", is(equalTo(expected)))
    	// @formatter:on
    }

    private static void putStatus(final String boxName, final String secret, final String boxControlType, final int boxControlId, final int status) {
        def endpoint = PUTSTATUS_ENDPOINT.replace('{boxName}', boxName).replace('{secret}', secret).replace('{boxControlType}', boxControlType).replace('{boxControlId}', boxControlId.toString()).replace('{status}', status.toString())
        println "PUT endpoint = $endpoint"
        // @formatter:off
        given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .when()
                .put(endpoint)
            .then()
                .assertThat()
                    .statusCode(HttpStatus.SC_OK)
                    .body("message", is('ok'))
    	// @formatter:on
    }
}
