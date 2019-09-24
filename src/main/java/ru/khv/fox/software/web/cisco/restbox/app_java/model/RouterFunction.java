/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.model;

import lombok.*;
import org.springframework.http.HttpMethod;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.cisco.RestApiDTO;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.cisco.RestApiErrorResponse;

import java.util.Map;
import java.util.function.Function;

/**
 * Router Action descriptor.
 */
@Value
@Builder(builderClassName = "RouterFunctionInternalBuilder", builderMethodName = "internalBuilder")
@EqualsAndHashCode(of = "name")
public class RouterFunction<Q extends RestApiDTO, T extends RestApiDTO, V> {
	public enum FunctionType {ACTION, READ}

	FunctionType type;          // action / read
	String name;                // afunc1, afunc2, ... ; rfunc1, rfunc2, ...
	@Nullable
	String descr;
	@Nullable
	String routerName;
	@Nullable
	Router device;
	@Nullable
	String uriPath;             // relative to base
	@Nullable
	HttpMethod requestMethod;  // get/put/post/delete
	@Nullable
	Q requestObject;            // request body
	@Nullable
	Class<T> responseClazz;     // to be extracted from response body
	@Nullable
	Function<T, V> mapFunction; // argument is response object
	@Nullable
	Function<RestApiErrorResponse, V> resourceNotFoundFunction; // argument is response object


	// Function packed with properties required to perform a request
	public boolean isExecutable() {
		return device != null && uriPath != null && requestMethod != null;
	}

	public boolean isRead() {
		return type == FunctionType.READ;
	}

	public boolean isAction() {
		return type == FunctionType.ACTION;
	}

	// -----------------------------------------------------------------------------------------------------------------

	// Builder support
	public static <Q extends RestApiDTO, T extends RestApiDTO, R> RouterFunctionBuilder<Q, T, R> builder(final Map<String, Router> routerMap) {
		return new RouterFunctionBuilder<>(routerMap);
	}

	@RequiredArgsConstructor
	public static class RouterFunctionBuilder<Q extends RestApiDTO, T extends RestApiDTO, R> extends RouterFunctionInternalBuilder<Q, T, R> {
		private final Map<String, Router> routerMap;

		@Override
		public RouterFunction<Q, T, R> build() {
			// Lookup and assign router reference by router id if not specified explicitly
			if (super.device == null && super.routerName != null) {
				val router = routerMap.get(super.routerName);
				Assert.notNull(router, "Router with name \"" + super.routerName + "\" does not exists");
				super.device(router);
			}
			return super.build();
		}
	}
}
