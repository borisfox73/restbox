/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.integrationTests;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.test.StepVerifier;
import ru.khv.fox.software.web.cisco.restbox.app_java.service.CiscoRestfulService;

import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

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
//public class IT51CiscoRestfulApiService3<Q extends RestApiDTO, T extends RestApiDTO, V> {
public class IT51CiscoRestfulApiService3 {

	@Autowired
	private CiscoRestfulService ciscoRestfulService;
	//	private CiscoRestfulService<Q, T, V> ciscoRestfulService;
	private static boolean initdone;


	@Before
	public void init() throws InterruptedException {
		if (!initdone) {
			// Set interface state for rfunc1/rfunc2
			System.out.println("exec afunc2");
			testFuncVoid("afunc2");
			Thread.sleep(10 * 1000L); // let interface status settle
			initdone = true;
		}
	}

	@Test
	public void testFuncRnone() {
		testFuncVoid("rnone");
	}

	@Test
	public void testFuncRfunc1() {
		testFuncValue("rfunc1", 0);
	}

	@Test
	public void testFuncRfunc2() {
		testFuncValue("rfunc2", 1);
	}

	@Test
	public void testFuncRfunc3() {
		testFuncValue("rfunc3", 1);
	}

	@Test
	public void testFuncRfunc4on() {
		testFuncVoid("afunc9");     // enable acl
		testFuncValue("rfunc4", 1);
	}

	@Test
	public void testFuncRfunc4off() {
		testFuncVoid("afunc10");    // disable acl
		testFuncValue("rfunc4", 0);
	}

	private void testFuncVoid(final String func) {
		StepVerifier.create(ciscoRestfulService.execFunction(func))
		            .verifyComplete();
	}

	private void testFuncValue(final String func, final int value) {
		StepVerifier.create(ciscoRestfulService.execFunction(func))
		            .assertNext(resultPair -> {
			            System.out.println("function " + func + " result pair: " + resultPair);
			            assertThat(resultPair, hasProperty("boxValue", is(optionalWithValue(allOf(instanceOf(Integer.class), equalTo(value))))));
		            })
		            .verifyComplete();
	}
}
