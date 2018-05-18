/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.lang.NonNull;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class RestApiAuthEntryPoint implements ServerAuthenticationEntryPoint {

	@NonNull private final ErrorWebExceptionHandler exceptionHandler;


	@NonNull
	@Override
	public Mono<Void> commence(@NonNull final ServerWebExchange exchange, @NonNull final AuthenticationException authException) {
		log.trace("custom auth entry point exception message: {}, cause message: {}", authException.getLocalizedMessage(), Optional.ofNullable(authException.getCause()).map(Throwable::getLocalizedMessage).orElse("<none>"));
		// invoke default exception handler with response status exception
		return exceptionHandler.handle(exchange, ResponseStatusExceptionWrapper.wrap(authException));
	}
}
