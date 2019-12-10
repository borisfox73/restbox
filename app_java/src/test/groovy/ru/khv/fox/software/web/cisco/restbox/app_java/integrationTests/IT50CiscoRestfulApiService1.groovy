/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.integrationTests

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import reactor.test.StepVerifier
import ru.khv.fox.software.web.cisco.restbox.app_java.model.cisco.RestApiErrorResponse
import ru.khv.fox.software.web.cisco.restbox.app_java.service.CiscoRestApiException
import ru.khv.fox.software.web.cisco.restbox.app_java.service.CiscoRestfulService

import static com.spotify.hamcrest.pojo.IsPojo.pojo
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*
import static org.hamcrest.junit.internal.ThrowableMessageMatcher.hasMessage

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
class IT50CiscoRestfulApiService1 {

	@Autowired
	private CiscoRestfulService ciscoRestfulService


	@Test
	void getHostname() {
		StepVerifier.create(ciscoRestfulService.getHostname("testcsr1"))
				.assertNext({
					System.out.println("host name service response: " + it)
					System.out.println("host name: " + it.getHostName())
					assertThat(it, hasProperty("hostName", equalTo("csr-wan")))
		            })
				.verifyComplete()
	}

	@Test
	void getOneUser() {
		StepVerifier.create(ciscoRestfulService.getUser("testcsr1", "rest"))
				.verifyErrorSatisfies({
					assertThat(it, allOf(instanceOf(CiscoRestApiException.class),
                                                                   hasProperty("errorResponse",
                                                                               is(pojo(RestApiErrorResponse.class).withProperty("errorCode", is(-1))
                                                                                                                  .withProperty("errorMessage", is("user rest not found"))
                                                                                                                  .withProperty("errorDetail", is(" ")))),
                                                                   hasMessage(equalTo("404 Not Found"))
					))
				})
	}

	@Test
	void getAllUsers() {
		StepVerifier.create(ciscoRestfulService.getUsers("testcsr1"))
		            .expectSubscription()
		            .thenRequest(10)
				.verifyComplete()
	}

	@Test
	void checkTokenAndReauthenticate() {
		// make any request to obtain authentication toke
		StepVerifier.create(ciscoRestfulService.getHostname("testcsr1"))
		            .expectNextCount(1)
				.verifyComplete()
		System.out.println("Check auth token")
		StepVerifier.create(ciscoRestfulService.checkAuthToken("testcsr1"))
				.assertNext({
					System.out.println("auth service response: " + it)
					assertThat(it, hasProperty("kind", equalTo("object#auth-token")))
		            })
				.verifyComplete()
		System.out.println("Re-Authenticate")
		StepVerifier.create(ciscoRestfulService.reAuthenticate("testcsr1"))
				.verifyComplete()
		System.out.println("Check auth token again")
		StepVerifier.create(ciscoRestfulService.checkAuthToken("testcsr1"))
				.assertNext({
					System.out.println("auth service response: " + it)
					assertThat(it, hasProperty("kind", equalTo("object#auth-token")))
		            })
				.verifyComplete()
		System.out.println("Invalidate auth token")
		StepVerifier.create(ciscoRestfulService.invalidateAuthToken("testcsr1"))
				.verifyComplete()
		System.out.println("token invalidated ok")
	}

	@Test
	void reauthenticateOne() {
		System.out.println("Invalidate auth token")
		StepVerifier.create(ciscoRestfulService.invalidateAuthToken("testcsr1"))
				.verifyComplete()
		System.out.println("Re-Authenticate again")
		StepVerifier.create(ciscoRestfulService.reAuthenticate("testcsr1"))
				.verifyComplete()
		System.out.println("Check auth token once more")
		StepVerifier.create(ciscoRestfulService.checkAuthToken("testcsr1"))
				.assertNext({
					System.out.println("auth service response: " + it)
					assertThat(it, hasProperty("kind", equalTo("object#auth-token")))
		            })
				.verifyComplete()
		System.out.println("Invalidate auth token at last")
		StepVerifier.create(ciscoRestfulService.invalidateAuthToken("testcsr1"))
				.verifyComplete()
		System.out.println("token invalidated ok")
	}

	@Test
	void reauthenticateAll() {
		System.out.println("Re-Authenticate All routers")
		StepVerifier.create(ciscoRestfulService.reAuthenticateAll())
				.verifyComplete()
	}
}
