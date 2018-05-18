/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.controller;

import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
public class Default {

	// Default controller for otherwise not matched urls
	@RequestMapping
	public Mono<Void> endpointUnknown(@NonNull final ServerWebExchange exchange) {
		// TODO what is a best practice ?
		//throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found");
		return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "No handler found for " + exchange.getRequest().getPath().value()));
	}
}
