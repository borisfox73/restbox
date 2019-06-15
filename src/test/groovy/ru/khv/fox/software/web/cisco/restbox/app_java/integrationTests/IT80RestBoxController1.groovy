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
//@TestPropertySource(properties = [
//        "app.config.users[0].username=testuser",
//        "app.config.users[0].password=testpass",
//        "app.config.users[0].roles=USER,ADMIN",
//        "app.config.jwt.time-to-live=PT1M"
//])
@ActiveProfiles("test")
class IT80RestBoxController1 {

//	@ContextConfiguration
//	public class SpringTestConfig {	}

	private static final String GETSTATUS_ENDPOINT = "/api/get/{boxName}/{secret}/{boxControlType}/{boxControlId}"
	private static final String PUTSTATUS_ENDPOINT = "/api/put/{boxName}/{secret}/{boxControlType}/{boxControlId}/{status}"

	@LocalServerPort
	private int serverPort


	@Before
	void initRestAssured() {
		RestAssured.port = serverPort
		RestAssured.filters(new ResponseLoggingFilter())
		RestAssured.filters(new RequestLoggingFilter())
	}

	// TODO mock cisco api service to test actions

/*
	@Test
	void 'get status b1 button 0'() {
		getStatus('b1', 'cisco123', 'button', 0, 0)
	}

	@Test
	void 'get status b2 led 0'() {
		getStatus('b2', 'cisco456', 'led', 0, 0)
	}
*/

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
		def endpoint = GETSTATUS_ENDPOINT.replace('{boxName}', 'nonexistentbox').replace('{secret}', 'cisco123').replace('{boxControlType}', 'led').replace('{boxControlId}', "1")
		print "GET endpoint = $endpoint"
		// @formatter:off
        given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
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
		def endpoint = GETSTATUS_ENDPOINT.replace('{boxName}', 'b1').replace('{secret}', 'cisco123').replace('{boxControlType}', 'led').replace('{boxControlId}', "5")
		print "GET endpoint = $endpoint"
		// @formatter:off
        given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
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
		def endpoint = PUTSTATUS_ENDPOINT.replace('{boxName}', 'b1').replace('{secret}', 'cisco123').replace('{boxControlType}', 'led').replace('{boxControlId}', "5").replace('{status}', "1")
		print "PUT endpoint = $endpoint"
		// @formatter:off
        given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .when()
                .put(endpoint)
            .then()
                .assertThat()
                    .statusCode(HttpStatus.SC_NOT_FOUND)
                    .body("message", is("Control with type 'LED' and id 5 not found"))
    	// @formatter:on
	}

	@Test
	void 'authentication error'() {
		def endpoint = GETSTATUS_ENDPOINT.replace('{boxName}', 'b1').replace('{secret}', 'wrong').replace('{boxControlType}', 'led').replace('{boxControlId}', "5")
		print "GET endpoint = $endpoint"
		// @formatter:off
        given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .when()
                .get(endpoint)
            .then()
                .assertThat()
                    .statusCode(HttpStatus.SC_UNAUTHORIZED)
                    .body("message", is("auth_error"))
    	// @formatter:on
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
