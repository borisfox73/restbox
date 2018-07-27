/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.util;

import lombok.val;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.lang.NonNull;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.Map;
import java.util.Optional;

public class RestApiErrorAttributes extends DefaultErrorAttributes {

	public RestApiErrorAttributes(final boolean includeException) {
		super(includeException);
	}

	@NonNull
	@Override
	public Map<String, Object> getErrorAttributes(ServerRequest request,
	                                              boolean includeStackTrace) {
		val errorAttributes = super.getErrorAttributes(request, includeStackTrace);
		val error = getError(request);
		errorAttributes.put("reason", errorAttributes.get("message"));
		errorAttributes.put("message", determineMessage(error).orElse((String) errorAttributes.get("error")));
		errorAttributes.put("suppress_details", suppressDetails(error));
		return errorAttributes;
	}

	@NonNull
	private Optional<String> determineMessage(@NonNull Throwable error) {
		if (error instanceof RestApiException)
			return Optional.ofNullable(error.getMessage());
		return Optional.empty();
	}

	private boolean suppressDetails(@NonNull Throwable error) {
		return error instanceof RestApiException && error.getCause() == null;
	}
}
