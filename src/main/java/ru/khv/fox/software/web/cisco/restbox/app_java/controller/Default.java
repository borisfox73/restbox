/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Controller holding generic purpose endpoints and handlers.
 */
@Slf4j
@RestController
class Default {

	// TODO cleanup.

	// User information retrieval endpoint
	@GetMapping(path = "userinfo", produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("isAuthenticated()")  // TODO redundant because protected by global authorization configuration
	public Mono<UserDetails> userinfo(@NonNull @AuthenticationPrincipal final Mono<UserDetails> user) {
		log.debug("user info: {}", user);
		// TODO may be explicit DTO would be more suitable. If this endpoint is not used by the frontend, just remove.
		// return as is for now
		return user;
	}

/*
	// Gracefully disable favicon
	@GetMapping("favicon.ico")
	@ResponseBody
	public Mono<Void> noFavicon() {
		return Mono.empty();
	}
*/
/*
	// Landing point for requests to endpoints not mapped otherwise
	@RequestMapping
	public Mono<Void> endpointUnknown(@NonNull final ServerWebExchange exchange) {
		// TODO what is the best practice to return error response from controllers ?
		//throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found");
		return Mono.error(new RestApiException("Resource with path " + exchange.getRequest().getPath().value() + " does not exist", HttpStatus.NOT_FOUND));
	}
*/
}
