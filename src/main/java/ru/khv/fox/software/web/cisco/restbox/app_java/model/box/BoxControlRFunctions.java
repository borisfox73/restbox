/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.model.box;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public enum BoxControlRFunctions {
	// type should be LED
	RNONE;

	// for serialization and deserialization
	@Nullable
	@JsonCreator
	private static BoxControlRFunctions fromJson(@Nullable final String value) {
		return value != null ? BoxControlRFunctions.valueOf(value.toUpperCase()) : null;
	}

	@NonNull
	@JsonValue
	private String toJson() {
		return this.name().toLowerCase();
	}
}
