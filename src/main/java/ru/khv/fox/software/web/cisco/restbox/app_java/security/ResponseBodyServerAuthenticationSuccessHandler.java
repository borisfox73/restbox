/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.security;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import ru.khv.fox.software.web.cisco.restbox.app_java.util.WebResponseHelper;

import java.util.function.Function;

/**
 * Authentication Success handler returning request body with JSON representation of the JWT data transfer object.
 * Used in the login endpoint filter configuration.
 */
@RequiredArgsConstructor
public class ResponseBodyServerAuthenticationSuccessHandler implements ServerAuthenticationSuccessHandler {
	@NonNull private final WebResponseHelper webResponseHelper;
	@NonNull private final Function<Authentication, Object> responseMapper;
	@NonNull private final HttpStatus status;


	@Override
	public Mono<Void> onAuthenticationSuccess(final WebFilterExchange webFilterExchange, final Authentication authentication) {
		return ServerResponse.status(status)
		                     .contentType(MediaType.APPLICATION_JSON_UTF8)
		                     .body(BodyInserters.fromObject(responseMapper.apply(authentication)))
		                     .flatMap(response -> webResponseHelper.write(webFilterExchange.getExchange(), response));
	}
}
