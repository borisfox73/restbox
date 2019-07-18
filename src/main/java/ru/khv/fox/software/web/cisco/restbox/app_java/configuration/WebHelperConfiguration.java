/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.configuration;

import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.result.view.ViewResolver;
import ru.khv.fox.software.web.cisco.restbox.app_java.util.RestApiErrorAttributes;
import ru.khv.fox.software.web.cisco.restbox.app_java.util.WebRequestHelper;
import ru.khv.fox.software.web.cisco.restbox.app_java.util.WebResponseHelper;

import java.util.Collections;
import java.util.List;

// Resembles ErrorWebFluxAutoConfiguration class.
// Overrides autoconfigured beans from there, and creates web request/response handling helpers.
// Web helpers are required for authentication processing with data exchange in JSON format.
@Configuration
class WebHelperConfiguration {

	private final ServerProperties serverProperties;
	//	    private final ApplicationContext applicationContext;
//	    private final ResourceProperties resourceProperties;
	private final List<ViewResolver> viewResolvers;
	private final ServerCodecConfigurer serverCodecConfigurer;


	public WebHelperConfiguration(ServerProperties serverProperties,
//	                                  ResourceProperties resourceProperties,
                                  ObjectProvider<List<ViewResolver>> viewResolversProvider,
                                  ServerCodecConfigurer serverCodecConfigurer) {
//	                                  ApplicationContext applicationContext) {
		this.serverProperties = serverProperties;
//		    this.applicationContext = applicationContext;
//		    this.resourceProperties = resourceProperties;
		this.viewResolvers = viewResolversProvider
				.getIfAvailable(Collections::emptyList);
		this.serverCodecConfigurer = serverCodecConfigurer;
	}

	@Bean
	RestApiErrorAttributes errorAttributes() {
		return new RestApiErrorAttributes(serverProperties.getError().isIncludeException());
	}
/*
	@Bean
	@Order(Ordered.HIGHEST_PRECEDENCE)
	RestApiErrorWebExceptionHandler errorWebExceptionHandler(final ErrorAttributes errorAttributes) {
		val exceptionHandler = new RestApiErrorWebExceptionHandler(errorAttributes, resourceProperties,
		                                                           serverProperties.getError(), applicationContext);
		exceptionHandler.setViewResolvers(viewResolvers);
		exceptionHandler.setMessageWriters(serverCodecConfigurer.getWriters());
		exceptionHandler.setMessageReaders(serverCodecConfigurer.getReaders());
		return exceptionHandler;
	}
*/

	@Bean
	WebResponseHelper webResponseHelper() {
		val responseHandler = new WebResponseHelper();
		responseHandler.setViewResolvers(viewResolvers);
		responseHandler.setMessageWriters(serverCodecConfigurer.getWriters());
		return responseHandler;
	}

	@Bean
	WebRequestHelper webRequestHelper() {
		return new WebRequestHelper(serverCodecConfigurer);
	}
}
