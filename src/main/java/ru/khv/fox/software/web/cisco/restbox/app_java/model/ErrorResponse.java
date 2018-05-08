/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.springframework.lang.NonNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

// TODO craft according to frontend requirements.
@Value
@AllArgsConstructor
public class ErrorResponse {
	@JsonProperty("error")
	@JsonPropertyDescription("Error details object")
	@NonNull
	private final ErrorDetails errorDetails;

	@Value
	@AllArgsConstructor
	public static class ErrorDetails {
		@JsonProperty
		@JsonPropertyDescription("Error code")
		private final int code;
		@JsonProperty
		@JsonPropertyDescription("Message containing error cause description")
		private final String reason;
	}

	@Nonnull
	public static ErrorResponse create(final int code, @Nullable final String reason) {
		return new ErrorResponse(new ErrorResponse.ErrorDetails(code, reason));
	}
}
