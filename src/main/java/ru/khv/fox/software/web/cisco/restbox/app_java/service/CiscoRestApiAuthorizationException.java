/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.service;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Getter
class CiscoRestApiAuthorizationException extends RuntimeException {

	@Nullable
	private final HttpStatus httpStatus;


	CiscoRestApiAuthorizationException(@NonNull final HttpStatus status) {
		super(status.value() + " " + status.getReasonPhrase());
		this.httpStatus = status;
	}

	CiscoRestApiAuthorizationException(@Nullable final HttpStatus status, @Nullable final String message) {
		super(message);
		this.httpStatus = status;
	}
}