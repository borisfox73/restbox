/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.model.box;

import com.fasterxml.jackson.annotation.JsonValue;
import org.springframework.lang.NonNull;

public enum BoxControlOnOffFunctions {
	// type should be SWITCH, BUTTON or USONIC
	ANONE;

	// TODO is json conversion needed here ?
/*
	// for serialization
	@Nullable
	@JsonCreator
	private static BoxControlOnOffFunctions fromJson(@Nullable final String value) {
		return value != null ? BoxControlOnOffFunctions.valueOf(value.toUpperCase()) : null;
	}
*/

	// for deserialization
	@NonNull
	@JsonValue
	private String toJson() {
		return this.name().toLowerCase();
	}
}
