/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.security;

import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import reactor.core.publisher.Mono;

/**
 * Authentication manager for Login (sign-on) requests.
 * Authenticates user credentials by the configured UserDetails Authentication Manager,
 * check account status, and create JWT holding user identity and authorities.
 */
@RequiredArgsConstructor
public class LoginReactiveAuthenticationManager implements ReactiveAuthenticationManager {

	@NonNull private static final AccountStatusUserDetailsChecker accountChecker = new AccountStatusUserDetailsChecker();
	@NonNull private final ReactiveAuthenticationManager userDetailsAuthenticationManager;
	@NonNull private final JwtService jwtService;


	@NonNull
	@Override
	public Mono<Authentication> authenticate(@NonNull final Authentication authentication) {
		return this.userDetailsAuthenticationManager.authenticate(authentication)
		                                            .map(Authentication::getPrincipal)
		                                            .cast(UserDetails.class)
		                                            .doOnNext(accountChecker::check)
		                                            .flatMap(jwtService::createJwt);
	}
}
