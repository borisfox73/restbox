/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.integrationTests

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import reactor.test.StepVerifier
import ru.khv.fox.software.web.cisco.restbox.app_java.service.CiscoRestfulService

import java.util.concurrent.TimeUnit

import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
class IT51CiscoRestfulApiService3 {

	@Autowired
	private CiscoRestfulService ciscoRestfulService
	private static boolean initDone


	@Before
	void init() throws InterruptedException {
		if (!initDone) {
			// Set interface state for rfunc1/rfunc2
			System.out.println("exec afunc2")
			testFuncVoid("afunc2")
			TimeUnit.SECONDS.sleep(10) // let interface status settle
			initDone = true
		}
	}

	@Test
	void testFuncRnone() {
		testFuncVoid("rnone")
	}

	@Test
	void testFuncRfunc1() {
		testFuncValue("rfunc1", 0)
	}

	@Test
	void testFuncRfunc2() {
		testFuncValue("rfunc2", 1)
	}

	@Test
	void testFuncRfunc3() {
		testFuncValue("rfunc3", 1)
	}

	@Test
	void testFuncRfunc4on() {
		testFuncVoid("afunc9")     // enable acl
		testFuncValue("rfunc4", 1)
	}

	@Test
	void testFuncRfunc4off() {
		testFuncVoid("afunc10")    // disable acl
		testFuncValue("rfunc4", 0)
	}

	private void testFuncVoid(final String func) {
		StepVerifier.create(ciscoRestfulService.execFunction(func))
				.verifyComplete()
	}

	private void testFuncValue(final String func, final int value) {
		StepVerifier.create(ciscoRestfulService.execFunction(func))
				.assertNext({
					System.out.println("function " + func + " result pair: " + it)
					assertThat(it, hasProperty("boxValue", is(optionalWithValue(allOf(instanceOf(Integer.class), equalTo(value))))))
		            })
				.verifyComplete()
	}
}
