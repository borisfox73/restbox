/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.security;

import lombok.val;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.ProviderNotFoundException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class JwtReactiveAuthenticationManager implements ReactiveAuthenticationManager {

	@NonNull
	@Override
	public Mono<Authentication> authenticate(@NonNull final Authentication authentication) {
		// TODO сделать ReactiveUserDetailsService для JWT, в котором декодировать его и создавать объект Users.
		// выдавать исключения нужно в этой службе или здесь, и только подклассы AuthenticationException,
		// иначе не будет вызван authenticationFailureHandler.
		// а можно и не службу, а прямо здесь всё сделать.
		final String username = authentication.getName();
		return Mono.just(authentication)
		           .publishOn(Schedulers.parallel())
		           .filter(JwtAuthenticationToken.class::isInstance)
		           .switchIfEmpty(Mono.defer(() -> Mono.error(new ProviderNotFoundException("Authentication token is not a JWT"))))
		           .cast(JwtAuthenticationToken.class)
		           .filter(JwtAuthenticationToken::hasIdentity)
		           .switchIfEmpty(Mono.defer(() -> Mono.error(new ProviderNotFoundException("JWT has empty id or subject"))))
		           .map(c -> {
			           val principal = new User(c.getName(), "", c.getAuthorities());
			           principal.eraseCredentials();
			           return new JwtAuthenticationToken(c.getId(), principal, c.getAuthorities());
		           });
	}
}

