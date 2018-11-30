/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.integrationTests

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import ru.khv.fox.software.web.cisco.restbox.app_java.service.CiscoRestfulService

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
class IT50CiscoRestfulApiService {

	@Autowired
	private CiscoRestfulService ciscoRestfulService

	@Test
	void 'misc test chain'() {
		println "cisco restful service: ${ciscoRestfulService}"
/*
		def authServiceResponse = ciscoRestfulService.authenticate("testcsr1").block()
		println "auth response: ${authServiceResponse}"
		def tokenCheckResponse = ciscoRestfulService.checkAuthToken("testcsr1").block()
		println "token check response: ${tokenCheckResponse}"
*/
		def hostnameServiceResponse = ciscoRestfulService.getHostname("testcsr1").block()
		println "host name response: ${hostnameServiceResponse}"
		println "host name: ${hostnameServiceResponse.getHostName()}"
		ciscoRestfulService.invalidateAuthToken("testcsr1").block()
		println "token invalidated ok"
		//def tokenCheckResponse2 = ciscoRestfulService.checkAuthToken("testcsr1").block();
		//println "token check2 response: ${tokenCheckResponse2}"
	}

}
