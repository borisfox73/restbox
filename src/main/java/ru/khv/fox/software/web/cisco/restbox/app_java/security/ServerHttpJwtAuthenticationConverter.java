/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.security;

import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.function.Function;

public class ServerHttpJwtAuthenticationConverter implements Function<ServerWebExchange, Mono<Authentication>> {
	private static final String TOKEN_PREFIX = "Bearer ";


	@NonNull
	@Override
	public Mono<Authentication> apply(@NonNull final ServerWebExchange exchange) {
		// extract encoded JWT from authorization header
		return Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
		           .filter(s -> s.startsWith(TOKEN_PREFIX))
		           .map(s -> s.substring(TOKEN_PREFIX.length()))
		           .filter(StringUtils::hasText)
		           .map(JwtAuthenticationToken::new);
	}
}

