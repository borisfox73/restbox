/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.unitTests

import org.junit.Test
import ru.khv.fox.software.web.cisco.restbox.app_java.model.box.BoxControlAction
import ru.khv.fox.software.web.cisco.restbox.app_java.model.box.BoxControlType

import static org.assertj.core.api.Assertions.assertThat
import static org.hamcrest.core.Is.is

class UT10BoxControlType {

	@Test
	void 'switch action'() {
		commonBinarySensor(BoxControlType.SWITCH)
	}

	@Test
	void 'switch button'() {
		commonBinarySensor(BoxControlType.BUTTON)
	}

	@Test
	void 'switch usonic'() {
		def boxControlType = BoxControlType.USONIC
		assertThat(boxControlType.getAction(0), is(BoxControlAction.NOOP))
		assertThat(boxControlType.getAction(5), is(BoxControlAction.NOOP))
		assertThat(boxControlType.getAction(6), is(BoxControlAction.OFF))
		assertThat(boxControlType.getAction(10), is(BoxControlAction.OFF))
		assertThat(boxControlType.getAction(49), is(BoxControlAction.OFF))
		assertThat(boxControlType.getAction(50), is(BoxControlAction.NOOP))
		assertThat(boxControlType.getAction(51), is(BoxControlAction.ON))
		assertThat(boxControlType.getAction(70), is(BoxControlAction.ON))
		assertThat(boxControlType.getAction(99), is(BoxControlAction.ON))
		assertThat(boxControlType.getAction(100), is(BoxControlAction.NOOP))
		assertThat(boxControlType.getAction(120), is(BoxControlAction.NOOP))
	}

	@Test(expected = UnsupportedOperationException.class)
	void 'switch led'() {
		BoxControlType.LED.getAction(0)
	}

	private static void commonBinarySensor(final BoxControlType boxControlType) {
		assertThat(boxControlType.getAction(0), is(BoxControlAction.OFF))
		assertThat(boxControlType.getAction(1), is(BoxControlAction.ON))
		assertThat(boxControlType.getAction(2), is(BoxControlAction.NOOP))
	}
}
