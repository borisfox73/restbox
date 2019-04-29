/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.security.Principal;

// TODO test endpoints.
@Slf4j
@RestController
public class TestController {

	@GetMapping("css/hello")
	public Mono<String> cssHello() {
		return Mono.just("Hello I'm secret data");
	}

	@PostMapping("post")
	public Mono<String> testPost() {
		return Mono.just("Hello it is post request");
	}

	@GetMapping("customHeader")
	public Mono<String> customHeader(@RequestHeader("x-custom-header") Mono<String> customHeader) {
		return customHeader;
	}

	@GetMapping("/greet")
	public Mono<String> greet(@NonNull final Mono<Principal> principal) {
		return principal
				.map(Principal::getName)
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
	public Mono<UserDetails> userinfo(@NonNull @AuthenticationPrincipal final Mono<UserDetails> user) {
		log.debug("user info: {}", user);
		// TODO may be explicit DTO would be more suitable. If this endpoint is not used by the frontend, just remove.
		// return as is for now
		return user;
	}

	@GetMapping(path = "jsontest", produces = MediaType.APPLICATION_JSON_VALUE)
// TODO method security does not work in 2.0.5 - exception handlers didn't get invoked
//	@PreAuthorize("isFullyAuthenticated() and hasRole('ADMIN')")
	public Mono<Principal> jsonTest(@NonNull final Mono<Principal> principal) {
		log.debug("principal: {}", principal);
		return principal;
	}
}
