/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.Router;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.box.Box;

import java.util.Collection;
import java.util.stream.Collectors;

@Configuration
class BoxAndRoutersConfiguration {

	// Session- and request-scoped beans cannot be used with Spring WebFlux
	// https://stackoverflow.com/questions/46540983/session-and-request-scopes-with-spring-webflux
	// Try to stay with application-wide state.
	//@Scope(value="session", proxyMode = ScopedProxyMode.TARGET_CLASS)
	//@SessionScope
	@Bean
	static Collection<Box> boxes(@NonNull final AppProperties appProperties) {
		// Use configuration properties to instantiate dynamic box objects holding runtime state
		return appProperties.getBoxcontrol().stream().map(Box::getInstance).collect(Collectors.toSet());
	}

	@Bean
	static Collection<Router> routers(@NonNull final AppProperties appProperties) {
		// Use configuration properties to instantiate dynamic router objects holding runtime state
		return appProperties.getRouters()
		                    .entrySet()
		                    .stream()
		                    .map(entry -> Router.getInstance(entry.getKey(), entry.getValue()))
		                    .collect(Collectors.toSet());
	}
}
