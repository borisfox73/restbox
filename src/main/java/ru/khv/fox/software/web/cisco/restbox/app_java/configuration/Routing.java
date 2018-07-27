/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

// TODO cleanup.

/**
 * Web controller routing component.
 * WebFlux.fn style.
 */
@Configuration
class Routing {
	// Routing function(s)
//	@Bean
//	public static RouterFunction<ServerResponse> routingFunction() {
//		// Handler function
//		//final HandlerFunction<ServerResponse> hello = request -> ok().body(Mono.just("Hello"), String.class);
//
//		// Setup routing
//		//return route(path("/"), hello);
//		//return route(GET("/"), Handlers::helloHandler);
//		return route(GET("/").and(accept(APPLICATION_JSON)), Handlers::helloHandler);
//	}

	// TODO: Not required after https://github.com/spring-projects/spring-boot/issues/9785 got resolved
	@Bean
	public static RouterFunction<ServerResponse> indexRouter(@Value("classpath:/static/index.html") final Resource indexHtml) {
		return route(GET("/"), request -> ok().contentType(MediaType.TEXT_HTML).syncBody(indexHtml));
	}
}
