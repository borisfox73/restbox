/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.LoginRequest;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.LoginResponse;
import ru.khv.fox.software.web.cisco.restbox.app_java.security.JwtUtility;


// TODO переделать на handler функцию
@Slf4j
@RequiredArgsConstructor
@RestController
class Login {
	@NonNull private final UserDetailsRepositoryReactiveAuthenticationManager authenticationManager;
	@NonNull private final AccountStatusUserDetailsChecker accountChecker = new AccountStatusUserDetailsChecker();
	@NonNull private final JwtUtility jwtUtility;


	@PostMapping(path = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@ResponseBody
	public Mono<LoginResponse> login(@RequestBody final LoginRequest loginParameters, @NonNull final ServerWebExchange exchange) {
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


	@GetMapping(path = "userinfo", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	@PreAuthorize("isAuthenticated()")  // TODO redundant
	public Mono<UserDetails> userinfo(@NonNull @AuthenticationPrincipal final UserDetails user) {
		log.debug("user info: {}", user);
		return Mono.just(user);
	}
}
