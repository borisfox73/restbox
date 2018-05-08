/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

/**
 * Wrapper for exceptions to provide specified HTTP Status for web exception handler.
 */
public class ResponseStatusExceptionWrapper extends ResponseStatusException {

	private ResponseStatusExceptionWrapper(final HttpStatus status, final String reason, final Throwable cause) {
		super(status, reason, cause);
	}

	private static ResponseStatusExceptionWrapper wrap(final Throwable exception, final HttpStatus status) {
		final String reason = Optional.ofNullable(exception.getCause()).map((e) -> e.getLocalizedMessage() + ": ").orElse("") +
		                      exception.getLocalizedMessage();
		return new ResponseStatusExceptionWrapper(status, reason, exception);
	}

	public static ResponseStatusExceptionWrapper wrap(final Throwable exception) {
		return wrap(exception, determineHttpStatus(exception));
	}

	// Empirically determine status based on exception type
	private static HttpStatus determineHttpStatus(final Throwable exception) {
		// TODO наполнить
		if (exception instanceof AuthenticationException)
			return HttpStatus.UNAUTHORIZED;
		else // catch-all value
			return HttpStatus.INTERNAL_SERVER_ERROR;
	}
}
