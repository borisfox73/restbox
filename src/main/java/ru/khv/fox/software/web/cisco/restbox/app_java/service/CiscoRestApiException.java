/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.service;

import lombok.Getter;
import lombok.ToString;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.cisco.RestApiErrorResponse;

@Getter
@ToString(callSuper = true)
public class CiscoRestApiException extends CiscoServiceException {

	private static final String ERROR_MESSAGE = "Cisco REST Service error";
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

	@Nullable
	@Override
	public String getErrorMessage() {
		return ERROR_MESSAGE;
	}

	@Nullable
	@Override
	public String getReason() {
		val sb = new StringBuilder(Integer.toString(httpStatus.value()))
				.append(" ")
				.append(httpStatus.getReasonPhrase());
		if (errorResponse != null) {
			sb.append(": ")
			  .append(errorResponse.getReason());
		}
		return sb.toString();
	}
}
