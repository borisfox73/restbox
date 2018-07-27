/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.util;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.core.ResolvableType;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.server.adapter.DefaultServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.function.Function;

/**
 * Web request handling helper methods.
 * Based on {@link DefaultServerWebExchange}
 */
@RequiredArgsConstructor
public class WebRequestHelper {
	@NonNull private final ServerCodecConfigurer configurer;


	/**
	 * Read JSON data from body and map to the specified type.
	 *
	 * @param request         HTTP request
	 * @param tClass          Target type class
	 * @param exceptionMapper Function to remap the exceptions (no remap if null)
	 * @param <T>             Result type
	 *
	 * @return Deserialized instance or error
	 */
	@NonNull
	public <T> Mono<T> readJsonData(@NonNull final ServerHttpRequest request,
	                                @NonNull final Class<? extends T> tClass,
	                                @Nullable Function<? super Throwable, ? extends Throwable> exceptionMapper) {

		try {
			if (exceptionMapper == null)
				exceptionMapper = e -> e;
			val tType = ResolvableType.forClass(tClass);
			val contentType = request.getHeaders().getContentType();
			if (MediaType.APPLICATION_JSON.isCompatibleWith(contentType)) {
				//noinspection unchecked
				return ((HttpMessageReader<T>) configurer.getReaders().stream()
				                                         .filter(reader -> reader.canRead(tType, MediaType.APPLICATION_JSON))
				                                         .findFirst()
				                                         .orElseThrow(() -> new IllegalStateException("No JSON HttpMessageReader.")))
						.readMono(tType, request, Collections.emptyMap())
//						.switchIfEmpty(Mono.empty())    // TODO need ?
						.onErrorMap(exceptionMapper)
						.cache();
			}
		} catch (InvalidMediaTypeException ex) {
			// Ignore
		}
		return Mono.empty();
	}

}
