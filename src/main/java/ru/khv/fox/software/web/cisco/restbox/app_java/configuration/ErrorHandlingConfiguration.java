/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.configuration;

import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import ru.khv.fox.software.web.cisco.restbox.app_java.util.RestApiErrorAttributes;

/**
 * Defines error handling properties.
 */
@Configuration
class ErrorHandlingConfiguration {

	// Error attributes format
	@Bean
	static RestApiErrorAttributes errorAttributes(@NonNull final ServerProperties serverProperties) {
		return new RestApiErrorAttributes(serverProperties.getError().isIncludeException());
	}
}
