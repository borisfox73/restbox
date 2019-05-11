/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.service;

import lombok.ToString;
import org.springframework.lang.Nullable;

@ToString
public
class CiscoServiceException extends RuntimeException {

	CiscoServiceException(final String message) {
		super(message);
	}

	@Nullable
	public String getErrorMessage() {
		return null;
	}

	@Nullable
	public String getReason() {
		return null;
	}
}
