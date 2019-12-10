/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.security.Principal;

@Slf4j
@RestController
@Profile("test")
public class TestController {

	@GetMapping("/greet")
	public Mono<String> greet(final Mono<Principal> principal) {
		return principal.map(Principal::getName)
		                .map(name -> String.format("Hello, %s", name));
	}

	// User information retrieval endpoint
	@GetMapping(path = "userinfo", produces = MediaType.APPLICATION_JSON_VALUE)
	// Reactive Method Security does not used in this app because webflux security exception handlers
	// (authentication endpoint and access denied) didn't get invoked at the stage method security kicks in/
	// Method security implemented not in the web filters layer but with the PrePostAdviceReactiveMethodInterceptor,
	// which throws exception that can be intercepted using ControllerAdvice in ErrorHandler class.
	// At least in Spring 5.0.8 / Spring Boot 2.0.5.
//	@PreAuthorize("isAuthenticated()")
	public Mono<UserDetails> userinfo(@AuthenticationPrincipal final Mono<UserDetails> user) {
		log.debug("user info: {}", user);
		return user;
	}

	@GetMapping(path = "jsontest", produces = MediaType.APPLICATION_JSON_VALUE)
//	@PreAuthorize("isFullyAuthenticated() and hasRole('ADMIN')")
	public Mono<Principal> jsonTest(final Mono<Principal> principal) {
		log.debug("principal: {}", principal);
		return principal;
	}
}
