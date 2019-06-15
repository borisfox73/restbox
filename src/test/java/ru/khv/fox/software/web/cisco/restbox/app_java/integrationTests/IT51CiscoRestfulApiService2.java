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
import ru.khv.fox.software.web.cisco.restbox.app_java.service.CiscoRestfulService;
import ru.khv.fox.software.web.cisco.restbox.app_java.service.CiscoServiceException;

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
//public class IT51CiscoRestfulApiService2<Q extends RestApiDTO, T extends RestApiDTO, V> {
public class IT51CiscoRestfulApiService2 {

	@Autowired
	private CiscoRestfulService ciscoRestfulService;
//	private CiscoRestfulService<Q, T, V> ciscoRestfulService;


	@Test
	public void testFuncAnone() {
		testFuncVoid("anone");
	}

	@Test
	public void testFuncAfunc1() {
		testFuncVoid("afunc1");
	}

	@Test
	public void testFuncAfunc2() {
		testFuncVoid("afunc2");
	}

	@Test
	public void testFuncAfunc3() {
		testFuncVoid("afunc3");
	}

	@Test
	public void testFuncAfunc4() {
		testFuncVoid("afunc4");
	}

	@Test
	public void testFuncAfunc5() {
		testFuncVoid("afunc5");
	}

	@Test
	public void testFuncAfunc6() {
		testFuncVoid("afunc6");
	}

	@Test
	public void testFuncAfunc7() {
		testFuncVoid("afunc7");
	}

	@Test
	public void testFuncAfunc8() {
		testFuncVoid("afunc8");
	}

	@Test
	public void testFuncAfunc9() {
		testFuncVoid("afunc9");
	}

	@Test
	public void testFuncAfunc10() {
		testFuncVoid("afunc10");
	}

	@Test
	public void testFuncAfunc11() {
		testFuncVoid("afunc11");
	}

	@Test
	public void testFuncAfunc12() {
		testFuncVoid("afunc12");
	}

	@Test
	public void testFuncUndefined() {
		StepVerifier.create(ciscoRestfulService.execFunction("nonexistent"))
		            .verifyErrorSatisfies(e -> assertThat(e, allOf(instanceOf(CiscoServiceException.class),
		                                                           hasMessage(equalTo("Function \"nonexistent\" is undefined"))
		                                                          )));
	}

	private void testFuncVoid(final String func) {
		StepVerifier.create(ciscoRestfulService.execFunction(func))
		            .verifyComplete();
	}
}
