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
import ru.khv.fox.software.web.cisco.restbox.app_java.service.CiscoRestfulService
import ru.khv.fox.software.web.cisco.restbox.app_java.service.CiscoServiceException

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*
import static org.hamcrest.junit.internal.ThrowableMessageMatcher.hasMessage

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
class IT51CiscoRestfulApiService2 {

	@Autowired
	private CiscoRestfulService ciscoRestfulService


	@Test
	void testFuncAnone() {
		testFuncVoid("anone")
	}

	@Test
	void testFuncAfunc1() {
		testFuncVoid("afunc1")
	}

	@Test
	void testFuncAfunc2() {
		testFuncVoid("afunc2")
	}

	@Test
	void testFuncAfunc3() {
		testFuncVoid("afunc3")
	}

	@Test
	void testFuncAfunc4() {
		testFuncVoid("afunc4")
	}

	@Test
	void testFuncAfunc5() {
		testFuncVoid("afunc5")
	}

	@Test
	void testFuncAfunc6() {
		testFuncVoid("afunc6")
	}

	@Test
	void testFuncAfunc7() {
		testFuncVoid("afunc7")
	}

	@Test
	void testFuncAfunc8() {
		testFuncVoid("afunc8")
	}

	@Test
	void testFuncAfunc9() {
		testFuncVoid("afunc9")
	}

	@Test
	void testFuncAfunc10() {
		testFuncVoid("afunc10")
	}

	@Test
	void testFuncAfunc11() {
		testFuncVoid("afunc11")
	}

	@Test
	void testFuncAfunc12() {
		testFuncVoid("afunc12")
	}

	@Test
	void testFuncUndefined() {
		StepVerifier.create(ciscoRestfulService.execFunction("nonexistent"))
				.verifyErrorSatisfies({
					assertThat(it, allOf(instanceOf(CiscoServiceException.class),
		                                                           hasMessage(equalTo("Function \"nonexistent\" is undefined"))
					))
				})
	}

	private void testFuncVoid(final String func) {
		StepVerifier.create(ciscoRestfulService.execFunction(func))
				.verifyComplete()
	}
}
