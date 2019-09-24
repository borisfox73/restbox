/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.util;

import lombok.val;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.Map;
import java.util.Optional;

public class RestApiErrorAttributes extends DefaultErrorAttributes {

	public RestApiErrorAttributes(final boolean includeException) {
		super(includeException);
	}

	@Override
	public Map<String, Object> getErrorAttributes(ServerRequest request, boolean includeStackTrace) {
		val errorAttributes = super.getErrorAttributes(request, includeStackTrace);
		errorAttributes.put("reason", errorAttributes.get("message"));
		// for DefaultErrorWebExceptionHandler mapping
		errorAttributes.remove("message");
		return Map.of("message", determineMessage(getError(request)).orElse((String) errorAttributes.get("error")),
		              "status", errorAttributes.get("status"),  // for DefaultErrorWebExceptionHandler#getHttpStatus
		              "error", errorAttributes);
	}

	private Optional<String> determineMessage(Throwable error) {
		return Optional.ofNullable(error instanceof RestApiException ? error.getMessage() : null);
	}
}
