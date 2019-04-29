/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.service;

import lombok.ToString;

@ToString
class CiscoServiceException extends RuntimeException {

	CiscoServiceException(final String message) {
		super(message);
	}
}
