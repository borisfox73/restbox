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
			            System.out.println("host name response: " + hostnameServiceResponse);
			            System.out.println("host name: " + hostnameServiceResponse.getHostName());
			            assertThat(hostnameServiceResponse, hasProperty("hostName", equalTo("csr1000v-test")));
		            })
		            .verifyComplete();
		StepVerifier.create(ciscoRestfulService.invalidateAuthToken("testcsr1"))
		            .verifyComplete();
		System.out.println("token invalidated ok");
	}

}
