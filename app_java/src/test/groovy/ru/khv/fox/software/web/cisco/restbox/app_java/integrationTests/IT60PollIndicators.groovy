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
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import reactor.test.StepVerifier
import ru.khv.fox.software.web.cisco.restbox.app_java.PollIndicatorResources
import ru.khv.fox.software.web.cisco.restbox.app_java.model.box.Box
import ru.khv.fox.software.web.cisco.restbox.app_java.model.box.BoxControl
import ru.khv.fox.software.web.cisco.restbox.app_java.model.box.BoxControlType
import ru.khv.fox.software.web.cisco.restbox.app_java.service.CiscoRestfulService
import ru.khv.fox.software.web.cisco.restbox.app_java.service.RestBoxService

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*

@SuppressWarnings("OptionalGetWithoutIsPresent")
@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = [
		"app.config.boxcontrol[0].name=b1",
		"app.config.boxcontrol[0].secret=dummy",
		"app.config.boxcontrol[0].boxes[0].type=led",
		"app.config.boxcontrol[0].boxes[0].id=0",
		"app.config.boxcontrol[0].boxes[0].descr=red light",
		"app.config.boxcontrol[0].boxes[0].rfunc=rfunc1",
		"app.config.boxcontrol[0].boxes[1].type=led",
		"app.config.boxcontrol[0].boxes[1].id=1",
		"app.config.boxcontrol[0].boxes[1].descr=green light",
		"app.config.boxcontrol[0].boxes[1].rfunc=rfunc2",
		"app.config.boxcontrol[1].name=b2",
		"app.config.boxcontrol[1].secret=dummy",
		"app.config.boxcontrol[1].boxes[0].type=led",
		"app.config.boxcontrol[1].boxes[0].id=2",
		"app.config.boxcontrol[1].boxes[0].descr=blue light",
		"app.config.boxcontrol[1].boxes[0].rfunc=rfunc3",
])
@ActiveProfiles("test")
class IT60PollIndicators {
	@Autowired
	private CiscoRestfulService ciscoService
	//	private CiscoRestfulService<Q, T, Integer> ciscoService;
	@Autowired
	private RestBoxService boxService
	@Autowired
	private Collection<Box> boxes
	// Could not autowire because instantiation this component is disabled by test profile
	private PollIndicatorResources pollIndicatorResources


	@Before
	void init() {
		// So instantiate it manually
		if (pollIndicatorResources == null)
			pollIndicatorResources = new PollIndicatorResources(ciscoService, boxService, boxes)
	}

	@Test
	void testBox0Led0() {
		testBoxLed("b1", 0, 0)
	}

	@Test
	void testBox0Led1() {
		testBoxLed("b1", 1, 1)
	}

	@Test
	void testBox1Led2() {
		testBoxLed("b2", 2, 1)
	}

	@Test
	void testPollCycle() {
		resetBoxControlStatus()
		StepVerifier.create(pollIndicatorResources.pollFlux())
		            .expectNextCount(3)
				.verifyComplete()
		assertThat(getBoxControl(getBox("b1"), 0), hasProperty("status", is(0)))
		assertThat(getBoxControl(getBox("b1"), 1), hasProperty("status", is(1)))
		assertThat(getBoxControl(getBox("b2"), 2), hasProperty("status", is(1)))
	}

	private void testBoxLed(String boxName, int boxControlId, int status) {
		resetBoxControlStatus()
		def box = getBox(boxName)
		def boxControl = getBoxControl(box, boxControlId)
		System.out.println("bc = " + boxControl)
		StepVerifier.create(pollIndicatorResources.pollBoxControl(box, boxControl))
				.assertNext({
					System.out.println("box control response: " + it)
					System.out.println("box control state: " + it.getStatus())
					assertThat(it, hasProperty("status", equalTo(status)))
		            })
				.verifyComplete()
	}

	private Box getBox(String boxName) {
		//noinspection GroovyAssignabilityCheck
		return boxes.find({ (it.getName() == boxName) })
	}

	private static BoxControl getBoxControl(Box box, int boxControlId) {
		return box.getControlByTypeAndId(BoxControlType.LED, boxControlId).get()
	}

	private void resetBoxControlStatus() {
		boxes.each { it.getBoxControls().each { it.setStatus(-1) } }
	}
}
