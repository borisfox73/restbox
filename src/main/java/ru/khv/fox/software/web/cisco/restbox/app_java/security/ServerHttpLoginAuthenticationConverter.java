/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.security;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.dto.LoginRequest;
import ru.khv.fox.software.web.cisco.restbox.app_java.util.RestApiException;
import ru.khv.fox.software.web.cisco.restbox.app_java.util.WebRequestHelper;

/**
 * Extract login endpoint data transfer object from JSON body of the request.
 * Encapsulate it in the user credentials token.
 */
@RequiredArgsConstructor
public class ServerHttpLoginAuthenticationConverter implements ServerAuthenticationConverter {

	@NonNull private final WebRequestHelper requestBodyHandler;


	@Override
	public Mono<Authentication> convert(@NonNull final ServerWebExchange exchange) {
		return Mono.just(exchange.getRequest())
		           .flatMap(request -> requestBodyHandler.readJsonData(request, LoginRequest.class,
		                                                               e -> new RestApiException("Login request body JSON deserialize error", HttpStatus.BAD_REQUEST, e)))
		           .map(login -> new UsernamePasswordAuthenticationToken(login.getUsername(), login.getPassword()));
	}
}

