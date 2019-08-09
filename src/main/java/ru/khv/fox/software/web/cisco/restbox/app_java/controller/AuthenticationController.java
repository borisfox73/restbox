/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.dto.LoginRequest;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.dto.LoginResponse;
import ru.khv.fox.software.web.cisco.restbox.app_java.security.LoginReactiveAuthenticationManager;

import javax.validation.Valid;

/**
 * Authentication controller.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
public class AuthenticationController {

	@NonNull
	private final LoginReactiveAuthenticationManager authenticationManager;


	/**
	 * Login endpoint.
	 * <p>
	 * Consumes API user credentials and produces JWT.
	 * <br>
	 * Should have open (unauthenticated) access.
	 * </p>
	 *
	 * @param loginRequest API user credentials object
	 *
	 * @return Response with JWT object
	 */
	@PostMapping(path = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	public Mono<LoginResponse> login(@RequestBody @Valid @NonNull final Mono<LoginRequest> loginRequest) {
		return loginRequest.doOnNext(x -> log.trace("x1 = {}", x)).map(login -> new UsernamePasswordAuthenticationToken(login.getUsername(), login.getPassword())).doOnNext(x -> log.trace("x2 = {}", x))
		                   .flatMap(authenticationManager::authenticate)
		                   .map(LoginResponse::from);
	}

}
