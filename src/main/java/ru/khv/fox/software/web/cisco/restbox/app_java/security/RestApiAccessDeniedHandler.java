/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.khv.fox.software.web.cisco.restbox.app_java.util.RestApiException;

import java.util.Optional;

/**
 * Access Denied handler returning error information encapsulated in the REST API error data transfer object.
 * Get invoked by the security framework when authorization configuration denies access (e.g. because of insufficient authorities).
 */
@Slf4j
@RequiredArgsConstructor
public class RestApiAccessDeniedHandler implements ServerAccessDeniedHandler {

	@NonNull private final ErrorWebExceptionHandler exceptionHandler;


	// Use configured exception handler to convert and publish the response.
	// See HttpStatusServerAccessDeniedHandler#handle for an example of "manual" response construction and publishing.
	@NonNull
	@Override
	public Mono<Void> handle(@NonNull final ServerWebExchange exchange, @NonNull final AccessDeniedException accessDeniedException) {
		return Mono.defer(() -> {
			log.trace("custom access denied handler exception message: {}, cause message: {}", accessDeniedException.getLocalizedMessage(), Optional.ofNullable(accessDeniedException.getCause()).map(Throwable::getLocalizedMessage).orElse("<none>"));
			return exceptionHandler.handle(exchange, new RestApiException("access denied error", HttpStatus.FORBIDDEN, accessDeniedException));
		});
	}
}
