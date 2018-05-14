/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.controller;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.LoginRequest;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.LoginResponse;
import ru.khv.fox.software.web.cisco.restbox.app_java.security.JwtUtility;

import javax.annotation.Nonnull;

// TODO переделать на handler функцию
@RequiredArgsConstructor
@RestController
class Login {
	@Nonnull private final UserDetailsRepositoryReactiveAuthenticationManager authenticationManager;
	@Nonnull private final AccountStatusUserDetailsChecker accountChecker = new AccountStatusUserDetailsChecker();
	@Nonnull private final JwtUtility jwtUtility;


	@PostMapping(path = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	@ResponseStatus(HttpStatus.CREATED)
	public Mono<LoginResponse> login(@RequestBody final LoginRequest loginParameters, @Nonnull final ServerWebExchange exchange) {
		val token = new UsernamePasswordAuthenticationToken(loginParameters.getUsername(), loginParameters.getPassword());
		// authenticate only check user existence and password match
		// account and credentials validity state should be checked explicitly
		return authenticationManager.authenticate(token)
		                            .map(Authentication::getPrincipal)
		                            .cast(UserDetails.class)
		                            .doOnNext(accountChecker::check)
		                            .map(userDetails -> jwtUtility.createJwt(userDetails, exchange))
		                            .map(LoginResponse::of);
	}
}
