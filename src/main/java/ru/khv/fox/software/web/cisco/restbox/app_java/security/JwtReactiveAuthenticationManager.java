/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.security;

import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Parse and check JWT presented in the webrequest.
 * Excahnge it to authenticated token with user identity and authorities extracted from JWT claims.
 */
@RequiredArgsConstructor
public class JwtReactiveAuthenticationManager implements ReactiveAuthenticationManager {

	@NonNull private final JwtService jwtService;


	@NonNull
	@Override
	public Mono<Authentication> authenticate(@NonNull final Authentication authentication) {
		return Mono.just(authentication)
		           .publishOn(Schedulers.parallel())
		           .map(Authentication::getName)
		           .flatMap(jwtService::parseJwt)
		           .switchIfEmpty(Mono.defer(() -> Mono.error(new AuthenticationCredentialsNotFoundException("JWT has empty id or subject"))));
	}
}
