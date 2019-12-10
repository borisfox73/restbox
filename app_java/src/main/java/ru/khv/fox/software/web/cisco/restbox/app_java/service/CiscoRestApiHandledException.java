/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.service;

import lombok.ToString;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.cisco.RestApiErrorResponse;

@ToString
class CiscoRestApiHandledException extends CiscoRestApiException {

	CiscoRestApiHandledException(final HttpStatus status, @Nullable final RestApiErrorResponse errorResponse) {
		super(status, errorResponse);
	}
}
