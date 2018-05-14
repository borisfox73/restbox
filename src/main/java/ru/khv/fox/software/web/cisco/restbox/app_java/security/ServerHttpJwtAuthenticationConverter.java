/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Slf4j
public class ServerHttpJwtAuthenticationConverter implements Function<ServerWebExchange, Mono<Authentication>> {
	private static final String TOKEN_PREFIX = "Bearer ";
	@NonNull private final JwtUtility jwtUtility;


	public ServerHttpJwtAuthenticationConverter(@NonNull final JwtUtility jwtUtility) {
		this.jwtUtility = jwtUtility;
	}


	@NonNull
	@Override
	public Mono<Authentication> apply(@NonNull final ServerWebExchange exchange) {
/* TODO cleanup
		// extract JWT from authorization header
		val request = exchange.getRequest();
		val authorization = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
		if(authorization == null || !authorization.startsWith(TOKEN_PREFIX))
			return Mono.empty();
		val jwtToken = authorization.substring(TOKEN_PREFIX.length());
		if (!StringUtils.hasText(jwtToken))
			return Mono.empty();

		// parse JWT and extract claims
		try {
			return Mono.just(new JwtAuthenticationToken(jwtUtility.parseJwt(jwtToken)));
		} catch (Throwable t) {
			return Mono.error(t);
		}
*/
		// TODO check that exceptions in parseJwt are correclty converted to error mono
		// Исключения здесь обрабатываются неправильно. Возможно, следует разбирать JWT уже в менеджере, а не в конвертере, и генерировать исключения там?
		return Mono.just(exchange)
		           .flatMap(this::extractToken)
		           .map(jwtUtility::parseJwt)
/*
				   .flatMap(s -> {
					   try {
						   return Mono.just(jwtUtility.parseJwt(s));
					   } catch (Throwable t) {
					   	    log.debug("produce error from throwable", t);
						   return Mono.error(t);
					   }
				   })
*/
                   .map(JwtAuthenticationToken::new);
	}


	/*
	 * Extracts JWT from authorization header
	 */
	private Mono<String> extractToken(@NonNull final ServerWebExchange exchange) {
		return Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
		           .filter(s -> s.startsWith(TOKEN_PREFIX))
		           .map(s -> s.substring(TOKEN_PREFIX.length()))
		           .filter(StringUtils::hasText);
	}
}

