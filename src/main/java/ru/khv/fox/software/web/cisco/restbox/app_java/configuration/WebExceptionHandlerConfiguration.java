/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.configuration;

import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.result.view.ViewResolver;

import java.util.Collections;
import java.util.List;

@Configuration
class WebExceptionHandlerConfiguration {

	// Overrides autoconfigured one From ErrorWebFluxAutoConfiguration.
	// ErrorAttributes bean is DefaultErrorAttributes bean instantiated there too.
	@Bean
	ErrorWebExceptionHandler errorWebExceptionHandler(final ServerProperties serverProperties,
	                                                  final ResourceProperties resourceProperties,
	                                                  final ObjectProvider<List<ViewResolver>> viewResolversProvider,
	                                                  final ServerCodecConfigurer serverCodecConfigurer,
	                                                  final ApplicationContext applicationContext,
	                                                  final ErrorAttributes errorAttributes) {
		val viewResolvers = viewResolversProvider.getIfAvailable(Collections::emptyList);
		val exceptionHandler = new CustomErrorWebExceptionHandler(errorAttributes, resourceProperties,
		                                                          serverProperties.getError(), applicationContext);
		exceptionHandler.setViewResolvers(viewResolvers);
		exceptionHandler.setMessageWriters(serverCodecConfigurer.getWriters());
		exceptionHandler.setMessageReaders(serverCodecConfigurer.getReaders());
		return exceptionHandler;
	}
}
