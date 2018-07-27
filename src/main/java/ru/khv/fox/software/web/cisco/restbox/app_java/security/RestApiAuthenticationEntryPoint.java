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
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.khv.fox.software.web.cisco.restbox.app_java.util.RestApiException;

import java.util.Optional;

/**
 * Authentication Entry Point returning error information encapsulated in the REST API error data transfer object.
 * Get invoked by the security framework when access requires authentication but it hasn't been provided yet.
 */
@Slf4j
@RequiredArgsConstructor
public class RestApiAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {

	@NonNull private final ErrorWebExceptionHandler exceptionHandler;


	// Use configured exception handler to convert and publish the response.
	// See HttpStatusServerAccessDeniedHandler#handle for an example of "manual" response construction and publishing.
	@NonNull
	@Override
	public Mono<Void> commence(@NonNull final ServerWebExchange exchange, @NonNull final AuthenticationException authenticationException) {
		return Mono.defer(() -> {
			log.trace("custom auth entry point exception message: {}, cause message: {}", authenticationException.getLocalizedMessage(), Optional.ofNullable(authenticationException.getCause()).map(Throwable::getLocalizedMessage).orElse("<none>"));
			return exceptionHandler.handle(exchange, new RestApiException("authentication error", HttpStatus.UNAUTHORIZED, authenticationException));
		});
	}
}
