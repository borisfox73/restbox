/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.model.box;

import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@RequiredArgsConstructor
public enum BoxControlTypes {
	SWITCH(ControlKinds.SENSOR), BUTTON(ControlKinds.SENSOR), USONIC(ControlKinds.SENSOR), LED(ControlKinds.INDICATOR);

	private enum ControlKinds {SENSOR, INDICATOR}

	private final ControlKinds kind;

	public boolean isSensor() {
		return kind == ControlKinds.SENSOR;
	}

	public boolean isIndicator() {
		return kind == ControlKinds.INDICATOR;
	}

	// TODO add a tostring/valueof method to align representation with one from the box firmware
	// for serialization and deserialization

	@NonNull
	BoxControl getInstance(final int id, @Nullable final String description) {
		switch (kind) {
			case SENSOR:
				return new BoxControlSensor(this, id, description);
			case INDICATOR:
				return new BoxControlIndicator(this, id, description);
			default:
				throw new IllegalStateException("Sensor kind " + kind + " is not supported");
		}
	}
}
