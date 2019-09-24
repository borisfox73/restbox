/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.service;

import lombok.ToString;
import org.springframework.lang.Nullable;

@ToString
public class CiscoServiceException extends RuntimeException {

	@Nullable
	private final String reason;


	CiscoServiceException(final String message) {
		super(message);
		this.reason = null;
	}

	CiscoServiceException(String message, String reason, Throwable cause) {
		super(message, cause);
		this.reason = reason;
	}

	@Nullable
	public String getErrorMessage() {
		return getMessage();
	}

	@Nullable
	public String getReason() {
		return reason;
	}
}
