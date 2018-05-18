/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.security;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.stream.Collectors;

@RequiredArgsConstructor
public class JwtReactiveAuthenticationManager implements ReactiveAuthenticationManager {

	@NonNull private final JwtUtility jwtUtility;


	@NonNull
	@Override
	public Mono<Authentication> authenticate(@NonNull final Authentication authentication) {
		return Mono.just(authentication)
		           .publishOn(Schedulers.parallel())
		           .map(this::decodeJwt)
		           .filter(JwtClaims::hasIdentity)
		           .switchIfEmpty(Mono.defer(() -> Mono.error(new AuthenticationCredentialsNotFoundException("JWT has empty id or subject"))))
		           .map(JwtReactiveAuthenticationManager::createAuthToken);
	}

	@NonNull
	private JwtClaims decodeJwt(@NonNull final Authentication authentication) {
		// jwt-related exceptions thrown will be wrapped by authentication ones
		return jwtUtility.parseJwt(authentication.getName());
	}

	@NonNull
	private static JwtAuthenticationToken createAuthToken(@NonNull final JwtClaims jwtClaims) {
		// create user object to be used as a principal
		val id = jwtClaims.getId();
		val username = jwtClaims.getSubject();
		Assert.notNull(username, "JWT subject is null");
		val authorities = jwtClaims.getAuthorityNames().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
		val principal = new User(username, "", authorities);
		principal.eraseCredentials();
		// token will be created already in authenticated state
		return new JwtAuthenticationToken(id, principal, principal.getAuthorities());
	}
}
