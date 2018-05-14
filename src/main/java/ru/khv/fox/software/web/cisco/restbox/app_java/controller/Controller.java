/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.ErrorResponse;

import java.security.Principal;

@Slf4j
@RestController
public class Controller {

	// Default controller for otherwise not matched urls
	@GetMapping
	public Mono<String> get() {
		return Mono.just(String.valueOf(System.currentTimeMillis()));
	}

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

	@GetMapping(path = "jsontest", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
//	@PreAuthorize("isFullyAuthenticated()")
	public Mono<ErrorResponse> getJsonTest(@NonNull final Principal principal) {
		log.debug("principal: {}", principal);
		final ErrorResponse response = new ErrorResponse(new ErrorResponse.ErrorDetails(123, "Test principal = " + principal.toString()));
		log.debug("jsontest: composed message {}", response);

		return Mono.just(response);
	}

}
