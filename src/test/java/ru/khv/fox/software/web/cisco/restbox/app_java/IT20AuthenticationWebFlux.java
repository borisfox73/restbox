/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import ru.khv.fox.software.web.cisco.restbox.app_java.controller.TestController;

/*
 Spring WebFlux Testing cannot be used with Spring WebFlux Security due to autoconfiguration restrictions:

 43.3.10 Auto-configured Spring WebFlux Tests
 To test that Spring WebFlux controllers are working as expected, you can use the @WebFluxTest annotation.
 @WebFluxTest auto-configures the Spring WebFlux infrastructure and limits scanned beans to
 @Controller, @ControllerAdvice, @JsonComponent, Converter, GenericConverter, and WebFluxConfigurer.
 Regular @Component beans are not scanned when the @WebFluxTest annotation is used.
 */
@Ignore("WebFluxTest cannot be used for this")
@RunWith(SpringRunner.class)
@WebFluxTest(controllers = {TestController.class})
@ActiveProfiles("test")
public class IT20AuthenticationWebFlux {

	private static final String TEST_ENDPOINT = "/jsontest";

	@Autowired
	private WebTestClient webClient;

	@Test
	public void unauthenticated() {
		// @formatter:off
		webClient.get()
		            .uri(TEST_ENDPOINT)
		            .accept(MediaType.APPLICATION_JSON)
		         .exchange()
		            .expectStatus()
		                .isUnauthorized();
		// @formatter:on
	}
}
