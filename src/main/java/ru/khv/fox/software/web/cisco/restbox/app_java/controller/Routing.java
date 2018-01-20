/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.controller;

/**
 * Web controller routing component.
 * WebFlux.fn style.
 */
/*
@Configuration
public class Routing {
	// Routing function
	@Bean
	public static RouterFunction<ServerResponse> routingFunction() {
		// Handler function
		//final HandlerFunction<ServerResponse> hello = request -> ok().body(Mono.just("Hello"), String.class);

		// Setup routing
		//return route(path("/"), hello);
		//return route(GET("/"), Handlers::helloHandler);
		return route(GET("/").and(accept(APPLICATION_JSON)), Handlers::helloHandler);
	}
}
*/
