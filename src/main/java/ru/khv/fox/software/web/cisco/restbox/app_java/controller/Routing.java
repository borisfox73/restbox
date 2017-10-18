/*
 * Copyright (c) 2017 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.controller;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

/**
 * Web controller routing component.
 */
@Component
public final class Routing {
	// Routing function
	@Bean
	public static RouterFunction<ServerResponse> routingFunction() {
		// Handler function
		//final HandlerFunction<ServerResponse> hello = request -> ok().body(Mono.just("Hello"), String.class);

		// Setup routing
		//return route(path("/"), hello);
		return route(GET("/"), Handlers::helloHandler);
	}
}
