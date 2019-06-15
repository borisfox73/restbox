/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.model;

import lombok.*;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.cisco.RestApiDTO;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.cisco.RestApiErrorResponse;

import java.util.Map;
import java.util.function.Function;

/**
 * Router Action descriptor.
 */
// TODO generalize Object types (three: request, response, and callback arg)
@Value
@Builder(builderClassName = "RouterFunctionInternalBuilder", builderMethodName = "internalBuilder")
@EqualsAndHashCode(of = "name")
//public class RouterFunction<Q extends RestApiDTO, T extends RestApiDTO, E extends RestApiErrorDTO, V> {
public class RouterFunction<Q extends RestApiDTO, T extends RestApiDTO, V> {
	public enum FunctionType {ACTION, READ}

	@NonNull
	private FunctionType type;
	@NonNull
	private String name;    // afunc1, afunc2, ... ; rfunc1, rfunc2, ...
	@Nullable
	private String descr;
	@Nullable
	private String routerName;    // TODO move to configuration properties object
	@Nullable
	private Router device;
	@Nullable
	private String uriPath; // relative to base
	@Nullable
	private HttpMethod requestMethod;  // get/put/post/delete
	//	@Nullable
//	private Class<Q> requestClazz; // to be inserted to request body
	@Nullable
	private Q requestObject;   // request body
	@Nullable
	private Class<T> responseClazz; // to be extracted from response body
	@Nullable
	private final Function<T, V> mapFunction; // argument is response object
	// TODO design a way to map a "handled errors" (e.g. 404 NOT FOUND) to V value
	// need Predicate to match those errors, and a CiscoRestApiHandledException argument to mapper function
	// service-specific exception dependence is cumbersome, consider to introduce RouterHandledException type
	@Nullable
//	private final Function<E, V> resourceNotFoundFunction; // argument is response object
	private final Function<RestApiErrorResponse, V> resourceNotFoundFunction; // argument is response object

	// TODO consider supplying callback function into the action execution method in cisco service from controller
	//  e.g. to change restbox indicator state based on body retrieved by READ action.

	// TODO define methods

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
//	public static <Q extends RestApiDTO, T extends RestApiDTO, E extends RestApiErrorResponse, R> RouterFunctionBuilder<Q, T, R> builder(@NonNull final Map<String, Router> routerMap) {
	public static <Q extends RestApiDTO, T extends RestApiDTO, R> RouterFunctionBuilder<Q, T, R> builder(@NonNull final Map<String, Router> routerMap) {
		return new RouterFunctionBuilder<>(routerMap);
	}

	@RequiredArgsConstructor
//	public static class RouterFunctionBuilder<Q extends RestApiDTO, T extends RestApiDTO, E extends RestApiErrorResponse, R> extends RouterFunctionInternalBuilder<Q, T, E, R> {
	public static class RouterFunctionBuilder<Q extends RestApiDTO, T extends RestApiDTO, R> extends RouterFunctionInternalBuilder<Q, T, R> {
		@NonNull
		private final Map<String, Router> routerMap;

		@Override
//		public RouterFunction<Q, T, E, R> build() {
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
