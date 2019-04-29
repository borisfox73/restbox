/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.service;

import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.cisco.RestApiErrorResponse;

@Getter
@ToString(callSuper = true)
public class CiscoRestApiException extends CiscoServiceException {

	@NonNull
	private final HttpStatus httpStatus;
	@Nullable
	private RestApiErrorResponse errorResponse;


	CiscoRestApiException(@NonNull final HttpStatus status) {
		super(status.value() + " " + status.getReasonPhrase());
		this.httpStatus = status;
	}

	CiscoRestApiException(@NonNull final HttpStatus status, @Nullable final RestApiErrorResponse errorResponse) {
		this(status);
		this.errorResponse = errorResponse;
	}
}
