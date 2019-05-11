/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.util;

import lombok.Setter;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.lang.NonNull;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

/**
 * Web response handling helper methods.
 * Based on {@link AbstractErrorWebExceptionHandler}
 */
@Setter
public class WebResponseHelper {

	@NonNull private List<HttpMessageWriter<?>> messageWriters = Collections.emptyList();
	@NonNull private List<ViewResolver> viewResolvers = Collections.emptyList();


	/**
	 * Write the server response to specified exchange using configured message writers.
	 *
	 * @param exchange Exchange instance
	 * @param response Response object
	 *
	 * @return Void mono to indicate the completion
	 */
	// write the server response
	public Mono<? extends Void> write(@NonNull ServerWebExchange exchange,
	                                  @NonNull ServerResponse response) {
		// force content-type since writeTo won't overwrite response header values
		exchange.getResponse().getHeaders().setContentType(response.headers().getContentType());
		return response.writeTo(exchange, new ResponseContext());
	}

	/**
	 * Context holding writers configuration.
	 */
	private class ResponseContext implements ServerResponse.Context {

		@NonNull
		@Override
		public List<HttpMessageWriter<?>> messageWriters() {
			return WebResponseHelper.this.messageWriters;
		}

		@NonNull
		@Override
		public List<ViewResolver> viewResolvers() {
			return WebResponseHelper.this.viewResolvers;
		}
	}
}
