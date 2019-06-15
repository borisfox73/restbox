/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.integrationTests;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.test.StepVerifier;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.cisco.RestApiErrorResponse;
import ru.khv.fox.software.web.cisco.restbox.app_java.service.CiscoRestApiException;
import ru.khv.fox.software.web.cisco.restbox.app_java.service.CiscoRestfulService;

import static com.spotify.hamcrest.pojo.IsPojo.pojo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.junit.internal.ThrowableMessageMatcher.hasMessage;

@RunWith(SpringRunner.class)
@SpringBootTest
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@TestPropertySource(properties = [
//        "app.config.users[0].username=testuser",
//        "app.config.users[0].password=testpass",
//        "app.config.users[0].roles=USER,ADMIN",
//        "app.config.jwt.time-to-live=PT1M"
//])
@ActiveProfiles("test")
//public class IT50CiscoRestfulApiService1<Q extends RestApiDTO, T extends RestApiDTO, V> {
public class IT50CiscoRestfulApiService1 {

	@Autowired
	private CiscoRestfulService ciscoRestfulService;
//	private CiscoRestfulService<Q, T, V> ciscoRestfulService;


	@Test
	public void getHostname() {
		StepVerifier.create(ciscoRestfulService.getHostname("testcsr1"))
		            .assertNext(hostnameServiceResponse -> {
			            System.out.println("host name service response: " + hostnameServiceResponse);
			            System.out.println("host name: " + hostnameServiceResponse.getHostName());
			            assertThat(hostnameServiceResponse, hasProperty("hostName", equalTo("csr-wan")));
		            })
		            .verifyComplete();
	}

	@Test
	public void getOneUser() {
		StepVerifier.create(ciscoRestfulService.getUser("testcsr1", "rest"))
//		            .verifyError(CiscoRestApiException.class);
//		            .verifyErrorMessage("404 Not Found");
                    .verifyErrorSatisfies(e -> assertThat(e, allOf(instanceOf(CiscoRestApiException.class),
                                                                   hasProperty("errorResponse",
                                                                               is(pojo(RestApiErrorResponse.class).withProperty("errorCode", is(-1))
                                                                                                                  .withProperty("errorMessage", is("user rest not found"))
                                                                                                                  .withProperty("errorDetail", is(" ")))),
                                                                   hasMessage(equalTo("404 Not Found"))
                                                                  )));
/*
		            .assertNext(userServiceResponse -> {
			            System.out.println("user service response: " + userServiceResponse);
			            System.out.println("user name: " + userServiceResponse.getUsername());
			            assertThat(userServiceResponse, hasProperty("username", equalTo("rest")));
		            })
		            .verifyComplete();
*/
	}

	@Test
	public void getAllUsers() {
		StepVerifier.create(ciscoRestfulService.getUsers("testcsr1"))
		            .expectSubscription()
		            .thenRequest(10)
//		            .expectNextCount(1)
//		            .expectNextCount(2)
//		            .assertNext(userServiceResponse -> {
//			            System.out.println("user service response1: " + userServiceResponse);
//			            System.out.println("user name1: " + userServiceResponse.getUsername());
//			            assertThat(userServiceResponse, hasProperty("username", equalTo("adm")));
//		            })
/*
                    .assertNext(userServiceResponse -> {
	                    System.out.println("user service response2: " + userServiceResponse);
	                    System.out.println("user name2: " + userServiceResponse.getUsername());
	                    assertThat(userServiceResponse, hasProperty("username", equalTo("rest")));
                    })
*/
//		            .expectNextMatches(user -> "adm".equals(user.getUsername()))
//		            .expectNextMatches(user -> "rest".equals(user.getUsername()))
                    .verifyComplete();
	}

	@Test
	public void checkTokenAndReauthenticate() {
		// make any request to obtain authentication toke
		StepVerifier.create(ciscoRestfulService.getHostname("testcsr1"))
		            .expectNextCount(1)
		            .verifyComplete();
		System.out.println("Check auth token");
		StepVerifier.create(ciscoRestfulService.checkAuthToken("testcsr1"))
		            .assertNext(authServiceResponse -> {
			            System.out.println("auth service response: " + authServiceResponse);
			            assertThat(authServiceResponse, hasProperty("kind", equalTo("object#auth-token")));
		            })
		            .verifyComplete();
		System.out.println("Re-Authenticate");
		StepVerifier.create(ciscoRestfulService.reAuthenticate("testcsr1"))
		            .verifyComplete();
		System.out.println("Check auth token again");
		StepVerifier.create(ciscoRestfulService.checkAuthToken("testcsr1"))
		            .assertNext(authServiceResponse -> {
			            System.out.println("auth service response: " + authServiceResponse);
			            assertThat(authServiceResponse, hasProperty("kind", equalTo("object#auth-token")));
		            })
		            .verifyComplete();
		System.out.println("Invalidate auth token");
		StepVerifier.create(ciscoRestfulService.invalidateAuthToken("testcsr1"))
		            .verifyComplete();
		System.out.println("token invalidated ok");
	}

	@Test
	public void reauthenticateOne() {
		System.out.println("Invalidate auth token");
		StepVerifier.create(ciscoRestfulService.invalidateAuthToken("testcsr1"))
		            .verifyComplete();
		System.out.println("Re-Authenticate again");
		StepVerifier.create(ciscoRestfulService.reAuthenticate("testcsr1"))
		            .verifyComplete();
		System.out.println("Check auth token once more");
		StepVerifier.create(ciscoRestfulService.checkAuthToken("testcsr1"))
		            .assertNext(authServiceResponse -> {
			            System.out.println("auth service response: " + authServiceResponse);
			            assertThat(authServiceResponse, hasProperty("kind", equalTo("object#auth-token")));
		            })
		            .verifyComplete();
		System.out.println("Invalidate auth token at last");
		StepVerifier.create(ciscoRestfulService.invalidateAuthToken("testcsr1"))
		            .verifyComplete();
		System.out.println("token invalidated ok");
	}

	@Test
	public void reauthenticateAll() {
		System.out.println("Re-Authenticate All routers");
		StepVerifier.create(ciscoRestfulService.reAuthenticateAll())
		            .verifyComplete();
	}
}
