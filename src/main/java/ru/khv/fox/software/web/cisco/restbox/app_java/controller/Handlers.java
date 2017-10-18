/*
 * Copyright (c) 2017 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.controller;

import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.server.ServerResponse.ok;

/**
 * Request handlers.
 */
class Handlers {
	/**
	 * Simple "hello: output.
	 */
	static Mono<ServerResponse> helloHandler(final ServerRequest request) {
		return ok().body(Mono.just("Hello"), String.class);
	}
}
