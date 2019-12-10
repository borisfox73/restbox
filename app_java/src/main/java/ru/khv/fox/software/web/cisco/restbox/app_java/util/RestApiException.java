/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.util;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.web.server.ResponseStatusException;

/**
 * This application's REST API exception wrapper holding a brief message, a response status, and an optional error cause.
 */
@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class RestApiException extends ResponseStatusException {

	@Nullable
	String message;
	boolean suppressDetails;


	public RestApiException(@Nullable final String message, final HttpStatus status) {
		this(message, status, null);
	}

	public RestApiException(@Nullable final String message, final HttpStatus status, @Nullable final Throwable cause) {
		this(message, status, cause, determineReason(cause));
	}

	public RestApiException(@Nullable final String message, final HttpStatus status,
	                        @Nullable Throwable cause, @Nullable final String reason) {
		super(status, reason, cause);
		this.message = message;
		this.suppressDetails = reason == null && cause == null;
	}

	@Nullable
	private static String determineReason(@Nullable final Throwable exception) {
		if (exception == null)
			return null;
		val exceptionMessage = exception.getLocalizedMessage();
		val causeMessage = exception.getCause() != null ? exception.getCause().getLocalizedMessage() : null;
		return exceptionMessage != null && causeMessage != null && !causeMessage.equals(exceptionMessage) ?
		       causeMessage + ": " + exceptionMessage : exceptionMessage;
	}
}
