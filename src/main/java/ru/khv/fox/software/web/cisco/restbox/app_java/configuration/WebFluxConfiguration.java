/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.configuration;

import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

/**
 * Overall WebFlux configuration.
 */
@Configuration
class WebFluxConfiguration implements WebFluxConfigurer {

	// Add useful converters, especially StringToEnumIgnoringCaseConverterFactory
	@Override
	public void addFormatters(final FormatterRegistry registry) {
		ApplicationConversionService.configure(registry);
	}
}
