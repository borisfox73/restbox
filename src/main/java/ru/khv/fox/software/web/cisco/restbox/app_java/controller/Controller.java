/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.security.Principal;

// TODO test endpoints.
@Slf4j
@RestController
public class Controller {

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

	// TODO why authorization response is sent in text/plain instead of json ?
	@GetMapping(path = "jsontest", produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("isFullyAuthenticated() and hasRole('ADMIN')")
	public Mono<Principal> jsonTest(@NonNull final Mono<Principal> principal) {
		log.debug("principal: {}", principal);
		return principal;
	}

}
