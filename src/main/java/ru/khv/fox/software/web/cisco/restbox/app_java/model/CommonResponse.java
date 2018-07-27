/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Getter;
import lombok.ToString;
import org.springframework.lang.NonNull;

// Common API response with single message field.
@Getter
@ToString
public class CommonResponse {
	@JsonProperty
	@JsonPropertyDescription("Response message")
	@NonNull
	private final String message;

	CommonResponse(@NonNull final String message) {
		this.message = message;
	}

	public static CommonResponse with(@NonNull final String message) {
		return new CommonResponse(message);
	}
}
