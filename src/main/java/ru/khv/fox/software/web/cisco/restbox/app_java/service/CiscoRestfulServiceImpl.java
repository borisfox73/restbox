/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.service;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.Router;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.RouterFunction;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.cisco.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class CiscoRestfulServiceImpl<Q extends RestApiDTO, T extends RestApiDTO, V> implements CiscoRestfulService {
	private static final String HOSTNAME_SERVICES_ENDPOINT = "global/host-name";
	private static final String USERS_SERVICES_ENDPOINT = "global/local-users";
	private static final String USER_SERVICES_ENDPOINT = USERS_SERVICES_ENDPOINT + "/{user-name}";

	Map<String, CiscoRestApiClientState> routerStates;
	Map<String, RouterFunction<Q, T, V>> routerFunctions;


	CiscoRestfulServiceImpl(final Collection<Router> routerCollection,
	                        @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") final Map<String, RouterFunction<Q, T, V>> routerFunctions,
	                        final WebClient.Builder webClientBuilder,
	                        @Value("#{appProperties.ciscoRestApiUriTemplate}") final String baseUriTemplate) {
		this.routerFunctions = routerFunctions;
		routerStates = routerCollection.stream()
		                               .map(router -> new CiscoRestApiClientState(router, webClientBuilder, baseUriTemplate))
		                               .collect(Collectors.toUnmodifiableMap(state -> state.getRouter().getId(), state -> state));
		log.trace("Router client state map: {}", routerStates);
	}


	@Override
	public Map<String, Router> getRouters() {
		return routerStates.values()
		                   .stream()
		                   .map(CiscoRestApiClientState::getRouter)
		                   .collect(Collectors.toMap(Router::getId, router -> router));
	}

	private Mono<CiscoRestApiClientState> getClientState(final String routerId) {
		return Mono.justOrEmpty(routerStates.get(routerId));
	}

	@Override
	public Mono<AuthServiceResponse> checkAuthToken(final String routerId) {
		return getClientState(routerId)
				.flatMap(CiscoRestApiClientState::checkAuthenticationToken);
	}

	@Override
	public Mono<Void> invalidateAuthToken(final String routerId) {
		return getClientState(routerId)
				.flatMap(CiscoRestApiClientState::destroyAuthenticationToken);
	}

	@Override
	public Mono<Void> reAuthenticate(final String routerId) {
		return getClientState(routerId)
				.flatMap(CiscoRestApiClientState::reAuthentication);
	}

	@Override
	public Mono<Void> reAuthenticateAll() {
		return Flux.fromIterable(routerStates.values())
		           .flatMap(CiscoRestApiClientState::reAuthentication)
		           .then();
	}

	@Override
	public Mono<HostnameServiceResponse> getHostname(final String routerId) {
		return getClientState(routerId)
				.flatMap(clientState -> clientState.getWebClient()
				                                   .get()
				                                   .uri(HOSTNAME_SERVICES_ENDPOINT)
				                                   .retrieve()
				                                   .bodyToMono(HostnameServiceResponse.class));
	}

	@Override
	public Mono<UserServiceResponse> getUser(final String routerId, final String username) {
		return getClientState(routerId)
				.flatMap(clientState -> clientState.getWebClient()
				                                   .get()
				                                   .uri(USER_SERVICES_ENDPOINT, username)
				                                   .retrieve()
				                                   .onStatus(HttpStatus::isError, clientResponse -> clientResponse.bodyToMono(RestApiErrorResponse.class).flatMap(errorBody -> Mono.error(new CiscoRestApiException(clientResponse.statusCode(), errorBody))))
				                                   .bodyToMono(UserServiceResponse.class));
	}

	@Override
	public Flux<UserServiceResponse> getUsers(final String routerId) {
		return getClientState(routerId)
				.flatMap(clientState -> clientState.getWebClient()
				                                   .get()
				                                   .uri(USERS_SERVICES_ENDPOINT)
				                                   .retrieve()
				                                   .bodyToMono(UsersServiceResponse.class))
				.map(UsersServiceResponse::getUsers)
				.flatMapMany(Flux::fromIterable);
	}


	public Mono<ExecFunctionResultPair<? extends RestApiDTO, ? extends RestApiErrorDTO, ?>> execFunction(final String func) {
		Assert.notNull(func, "Function name must not be null");
		return Mono.justOrEmpty(routerFunctions.get(func))
		           .switchIfEmpty(Mono.defer(() -> Mono.error(new CiscoServiceException("Function \"" + func + "\" is undefined"))))
		           .filter(RouterFunction::isExecutable)
		           .flatMap(routerFunction -> {
			           // already has checked by isExecutable but reassert here to keep inspection happy
			           val device = routerFunction.getDevice();
			           assert device != null;
			           val clientStateMono = getClientState(device.getId());
			           val responseClazz = routerFunction.getResponseClazz();
			           val mapFunc = routerFunction.getMapFunction();
			           val errorFunc = routerFunction.getResourceNotFoundFunction();
			           Mono<ExecFunctionResultPair<? extends RestApiDTO, ? extends RestApiErrorDTO, V>> r;
			           if (responseClazz != null && mapFunc != null)
				           r = clientStateMono.flatMap(clientState -> prepareResponseSpec(clientState, routerFunction).bodyToMono(responseClazz))
				                              .map(rb -> ExecFunctionResultPair.of(rb, mapFunc.apply(rb)));
			           else // breaks type safety for mapping function
				           r = clientStateMono.flatMap(clientState -> prepareResponseSpec(clientState, routerFunction).bodyToMono((Class<? extends RestApiDTO>) (responseClazz != null ? responseClazz : RestApiUniversalDTO.class)))
				                              .map(rb -> ExecFunctionResultPair.of(rb, null));
			           r = r.onErrorMap(IOException.class, e -> new CiscoServiceException("Network error", e.getLocalizedMessage(), e));
			           return errorFunc != null ? r.onErrorResume(CiscoRestApiHandledException.class, e -> e.getErrorResponse() != null ? Mono.just(ExecFunctionResultPair.of(e.getErrorResponse(), errorFunc.apply(e.getErrorResponse()))) : Mono.error(e)) : r;
		           });
	}


	// deduplicate code in execFunction
	private static WebClient.ResponseSpec prepareResponseSpec(final CiscoRestApiClientState clientState,
	                                                          final RouterFunction func) {
		val requestMethod = func.getRequestMethod();
		val uriPath = func.getUriPath();
		val requestObject = func.getRequestObject();
		assert requestMethod != null && uriPath != null;
		log.trace("requestObject: {}", requestObject);
		val rbs = clientState.getWebClient()
		                     .method(requestMethod)
		                     .uri(uriPath);
		var r = (requestObject != null ? rbs.syncBody(requestObject) : rbs).retrieve();
		if (func.getResourceNotFoundFunction() != null)
			r = r.onStatus(status -> status == HttpStatus.NOT_FOUND, clientResponse -> clientResponse.bodyToMono(RestApiErrorResponse.class).flatMap(errorBody -> Mono.error(new CiscoRestApiHandledException(clientResponse.statusCode(), errorBody))));
		return r.onStatus(HttpStatus::isError, clientResponse -> clientResponse.bodyToMono(RestApiErrorResponse.class).flatMap(errorBody -> Mono.error(new CiscoRestApiException(clientResponse.statusCode(), errorBody))));
	}
}
