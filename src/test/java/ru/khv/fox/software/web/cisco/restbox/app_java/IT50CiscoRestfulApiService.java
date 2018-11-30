/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.test.StepVerifier;
import ru.khv.fox.software.web.cisco.restbox.app_java.service.CiscoRestfulService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;

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
public class IT50CiscoRestfulApiService {

	@Autowired
	private CiscoRestfulService ciscoRestfulService;

	@Test
	public void miscTestChain() {
		System.out.println("cisco restful service: " + ciscoRestfulService);
/*
		def authServiceResponse = ciscoRestfulService.authenticate("testcsr1").block()
		println "auth response: ${authServiceResponse}"
		def tokenCheckResponse = ciscoRestfulService.checkAuthToken("testcsr1").block()
		println "token check response: ${tokenCheckResponse}"
*/
/*
		def hostnameServiceResponse = ciscoRestfulService.getHostname("testcsr1").block()
		println "host name response: ${hostnameServiceResponse}"
		println "host name: ${hostnameServiceResponse.getHostName()}"
		ciscoRestfulService.invalidateAuthToken("testcsr1").block()
		println "token invalidated ok"
		//def tokenCheckResponse2 = ciscoRestfulService.checkAuthToken("testcsr1").block();
		//println "token check2 response: ${tokenCheckResponse2}"
*/
		StepVerifier.create(ciscoRestfulService.getHostname("testcsr1"))
		            .assertNext(hostnameServiceResponse -> {
			            System.out.println("host name service response: " + hostnameServiceResponse);
			            System.out.println("host name: " + hostnameServiceResponse.getHostName());
			            assertThat(hostnameServiceResponse, hasProperty("hostName", equalTo("csr1000v-test")));
		            })
		            .verifyComplete();
		StepVerifier.create(ciscoRestfulService.getUser("testcsr1", "rest"))
		            .assertNext(userServiceResponse -> {
			            System.out.println("user service response: " + userServiceResponse);
			            System.out.println("user name: " + userServiceResponse.getUsername());
			            assertThat(userServiceResponse, hasProperty("username", equalTo("rest")));
		            })
		            .verifyComplete();
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
                    .assertNext(userServiceResponse -> {
	                    System.out.println("user service response2: " + userServiceResponse);
	                    System.out.println("user name2: " + userServiceResponse.getUsername());
	                    assertThat(userServiceResponse, hasProperty("username", equalTo("rest")));
                    })
//		            .expectNextMatches(user -> "adm".equals(user.getUsername()))
//		            .expectNextMatches(user -> "rest".equals(user.getUsername()))
                    .verifyComplete();
		StepVerifier.create(ciscoRestfulService.checkAuthToken("testcsr1"))
		            .assertNext(authServiceResponse -> {
			            System.out.println("auth service response: " + authServiceResponse);
			            assertThat(authServiceResponse, hasProperty("kind", equalTo("object#auth-token")));
		            })
		            .verifyComplete();
		StepVerifier.create(ciscoRestfulService.invalidateAuthToken("testcsr1"))
		            .verifyComplete();
		System.out.println("token invalidated ok");
	}

}
