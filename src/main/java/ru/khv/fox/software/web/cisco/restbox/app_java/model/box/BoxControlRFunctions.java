/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.model.box;

import com.fasterxml.jackson.annotation.JsonValue;
import org.springframework.lang.NonNull;

public enum BoxControlRFunctions {
	// type should be LED
	RNONE;

	// TODO is json conversion needed here ?
/*
	// for serialization
	@Nullable
	@JsonCreator
	private static BoxControlRFunctions fromJson(@Nullable final String value) {
		return value != null ? BoxControlRFunctions.valueOf(value.toUpperCase()) : null;
	}
*/

	// for deserialization
	@NonNull
	@JsonValue
	private String toJson() {
		return this.name().toLowerCase();
	}
}
