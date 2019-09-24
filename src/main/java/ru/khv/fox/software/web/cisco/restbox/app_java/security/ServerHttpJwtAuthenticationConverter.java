/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.security;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Extract encoded JWT string from the Authorization header with Bearer scheme.
 * Encapsulate it in the raw JWT authentication container.
 */
public class ServerHttpJwtAuthenticationConverter implements ServerAuthenticationConverter {

	private static final String TOKEN_PREFIX = "Bearer ";


	@Override
	public Mono<Authentication> convert(final ServerWebExchange exchange) {
		return Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
		           .filter(s -> s.startsWith(TOKEN_PREFIX))
		           .map(s -> s.substring(TOKEN_PREFIX.length()))
		           .filter(StringUtils::hasText)
		           .map(JwtAuthenticationToken::new);
	}
}
