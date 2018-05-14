/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.lang.NonNull;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.khv.fox.software.web.cisco.restbox.app_java.controller.ResponseStatusExceptionWrapper;

import javax.annotation.Nonnull;
import java.util.Optional;

// TODO cleanup
@Slf4j
@RequiredArgsConstructor
@Component
public class RestApiAuthEntryPoint implements ServerAuthenticationEntryPoint {
	@NonNull private final ErrorWebExceptionHandler exceptionHandler;

	@Nonnull
	@Override
	public Mono<Void> commence(@Nonnull final ServerWebExchange exchange, @Nonnull final AuthenticationException authException) {
		//log.trace("custom auth entry point commence: exchange {}, authException:", exchange, authException);
		log.trace("custom auth entry point exception message: {}, cause message: {}", authException.getLocalizedMessage(), Optional.ofNullable(authException.getCause()).map(Throwable::getLocalizedMessage).orElse("<none>"));
		// Set response status and compose response body with error object
//		final String reason = Optional.ofNullable(authException.getCause()).map(Throwable::getLocalizedMessage).orElse("Authentication required") +
//		                      ": " + authException.getLocalizedMessage();
//		return exceptionHandler.handle(exchange, new ResponseStatusException(HttpStatus.UNAUTHORIZED, reason, authException));
		return exceptionHandler.handle(exchange, ResponseStatusExceptionWrapper.wrap(authException));
	}
}
