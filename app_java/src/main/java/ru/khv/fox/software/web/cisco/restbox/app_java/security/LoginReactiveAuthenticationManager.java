/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.security;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
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
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class LoginReactiveAuthenticationManager implements ReactiveAuthenticationManager {

	private static final AccountStatusUserDetailsChecker accountChecker = new AccountStatusUserDetailsChecker();
	ReactiveAuthenticationManager userDetailsAuthenticationManager;
	JwtService jwtService;


	@Override
	public Mono<Authentication> authenticate(final Authentication authentication) {
		return this.userDetailsAuthenticationManager.authenticate(authentication)
		                                            .map(Authentication::getPrincipal)
		                                            .cast(UserDetails.class)
		                                            .doOnNext(accountChecker::check)
		                                            .flatMap(jwtService::createJwt);
	}
}
