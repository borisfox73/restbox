/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.box.Box;

import java.util.Collection;
import java.util.stream.Collectors;

@Configuration
class BoxBeansConfiguration {

	// Session- and request-scoped beans cannot be used with Spring WebFlux
	// https://stackoverflow.com/questions/46540983/session-and-request-scopes-with-spring-webflux
	// Try to stay with application-wide state.
	//@Scope(value="session", proxyMode = ScopedProxyMode.TARGET_CLASS)
	//@SessionScope
	@Bean
	static Collection<Box> boxBeans(@NonNull final AppProperties appProperties) {
//		return appProperties.getBoxes();
		// Use configuration properties to instantiate dynamic box objects holding runtime state
		return appProperties.getBoxcontrol().stream().map(Box::getInstance).collect(Collectors.toSet());
	}
}
