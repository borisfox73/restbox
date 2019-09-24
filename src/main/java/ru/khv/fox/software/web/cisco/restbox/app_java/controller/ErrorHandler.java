/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.controller;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import ru.khv.fox.software.web.cisco.restbox.app_java.util.RestApiException;

import javax.validation.ConstraintViolationException;

/**
 * REST Endpoints exception handling.
 */
@Slf4j
@RestControllerAdvice
class ErrorHandler {

	// --- Application-specific ----------------------------------------------------------------------------------------

	// --- Authentication and authorization ----------------------------------------------------------------------------

	// Should be handled by security framework through the authentication entry point.
	@ExceptionHandler({AuthenticationException.class})
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public Mono<RestApiException> handleAuthenticationException(final AuthenticationException ex) {
		log.debug("authentication exception caught");
		return Mono.error(new RestApiException("authentication error", HttpStatus.UNAUTHORIZED, ex));
	}

	// Should be handled by security framework through the access denied handler.
	// This handler intercepts exceptions thrown in Advices, e.g. PrePostAdviceReactiveMethodInterceptor for method security
	@ExceptionHandler({AccessDeniedException.class})
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public Mono<RestApiException> handleAccessDeniedException(final AccessDeniedException ex) {
		log.debug("access denied exception caught");
		return Mono.error(new RestApiException("access denied error", HttpStatus.FORBIDDEN, ex));
	}

	// --- Validation and data-binding ---------------------------------------------------------------------------------

	// JSR-303 and Srping bean validation on DTO objects.
	@ExceptionHandler({ConstraintViolationException.class})
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public Mono<RestApiException> handleConstraintViolationExceptionException(final ConstraintViolationException ex) {
		log.debug("constraint violation exception caught");
		return Mono.error(new RestApiException("request constraint violation error", HttpStatus.BAD_REQUEST, ex));
	}

	// Method argument type mismatch or failed conversion.
	// Unknown enum value falls into this too.
	@ExceptionHandler({ServerWebInputException.class})
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public Mono<RestApiException> handleServerWebInputException(final ServerWebInputException ex) {
		log.debug("serverwebinput exception caught");
		val r1 = ex.getReason();
		val reason = r1 != null ? (ex.getMethodParameter() != null ? ex.getMethodParameter() + " '" + ex.getMethodParameter().getParameterName() + "': " + r1 : r1) : "request data binding error";
		return Mono.error(new RestApiException("request data conversion error", HttpStatus.BAD_REQUEST, ex, reason));
	}
}
