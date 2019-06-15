/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.service;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
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
//public class CiscoRestfulServiceImpl<Q extends RestApiDTO, T extends RestApiDTO, E extends RestApiErrorDTO, V> implements CiscoRestfulService<Q, T, E, V> {
//public class CiscoRestfulServiceImpl<Q extends RestApiDTO, T extends RestApiDTO, V> implements CiscoRestfulService<Q, T, V> {
public class CiscoRestfulServiceImpl<Q extends RestApiDTO, T extends RestApiDTO, V> implements CiscoRestfulService {
	private static final String HOSTNAME_SERVICES_ENDPOINT = "global/host-name";
	private static final String USERS_SERVICES_ENDPOINT = "global/local-users";
	private static final String USER_SERVICES_ENDPOINT = USERS_SERVICES_ENDPOINT + "/{user-name}";

	@NonNull
	private final Map<String, CiscoRestApiClientState> routerStates;
	@NonNull
//	private final Map<String, RouterFunction<Q, T, E, V>> routerFunctions;
	private final Map<String, RouterFunction<Q, T, V>> routerFunctions;


	CiscoRestfulServiceImpl(@NonNull final Collection<Router> routerCollection,
	                        @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
//	                        @NonNull final Map<String, RouterFunction<Q, T, E, V>> routerFunctions,
	                        @NonNull final Map<String, RouterFunction<Q, T, V>> routerFunctions,
	                        @NonNull final WebClient.Builder webClientBuilder) {/*,
	                        @Value("#{appProperties.traceWebClientRequests}") final boolean traceRequests,
	                        @Value("#{appProperties.sslIgnoreValidation}") final boolean sslIgnoreValidation) throws SSLException { */
		this.routerFunctions = routerFunctions;
		routerStates = routerCollection.stream()
		                               .map(router -> new CiscoRestApiClientState(router, webClientBuilder))
		                               .collect(Collectors.toUnmodifiableMap(state -> state.getRouter().getId(), state -> state));
		log.trace("Router client state map: {}", routerStates);
	}


	@NonNull
	@Override
	public Map<String, Router> getRouters() {
		return routerStates.values()
		                   .stream()
		                   .map(CiscoRestApiClientState::getRouter)
		                   .collect(Collectors.toMap(Router::getId, router -> router));
	}

	@NonNull
	private Mono<CiscoRestApiClientState> getClientState(@NonNull final String routerId) {
		return Mono.justOrEmpty(routerStates.get(routerId));
	}

/*
	@NonNull
	@Override
	public Mono<AuthServiceResponse> authenticate(@NonNull final String routerId) {
		return getClientState(routerId)
		           .flatMap(CiscoRestfulServiceImpl::obtainAuthenticationToken);
	}
*/

	@NonNull
	@Override
	public Mono<AuthServiceResponse> checkAuthToken(@NonNull final String routerId) {
		return getClientState(routerId)
				.flatMap(CiscoRestApiClientState::checkAuthenticationToken);
	}

	@NonNull
	@Override
	public Mono<Void> invalidateAuthToken(@NonNull final String routerId) {
		return getClientState(routerId)
				.flatMap(CiscoRestApiClientState::destroyAuthenticationToken);
	}

	@NonNull
	@Override
	public Mono<Void> reAuthenticate(@NonNull final String routerId) {
		return getClientState(routerId)
				.flatMap(CiscoRestApiClientState::reAuthentication);
	}

	@NonNull
	@Override
	public Mono<Void> reAuthenticateAll() {
		return Flux.fromIterable(routerStates.values())
		           .flatMap(CiscoRestApiClientState::reAuthentication)
		           .then();
	}

	@NonNull
	@Override
	public Mono<HostnameServiceResponse> getHostname(@NonNull final String routerId) {
		return getClientState(routerId)
				.flatMap(clientState -> clientState.getWebClient()
				                                   .get()
				                                   .uri(HOSTNAME_SERVICES_ENDPOINT)
				                                   .retrieve()
				                                   .bodyToMono(HostnameServiceResponse.class));
	}

	// TODO extract body on errors if present
	@NonNull
	@Override
	public Mono<UserServiceResponse> getUser(@NonNull final String routerId, @NonNull final String username) {
		return getClientState(routerId)
				.flatMap(clientState -> clientState.getWebClient()
				                                   .get()
				                                   .uri(USER_SERVICES_ENDPOINT, username)
				                                   .retrieve()
				                                   .onStatus(HttpStatus::isError, clientResponse -> clientResponse.bodyToMono(RestApiErrorResponse.class).flatMap(errorBody -> Mono.error(new CiscoRestApiException(clientResponse.statusCode(), errorBody))))
				                                   .bodyToMono(UserServiceResponse.class));
	}

	@NonNull
	@Override
	public Flux<UserServiceResponse> getUsers(@NonNull final String routerId) {
		return getClientState(routerId)
				.flatMap(clientState -> clientState.getWebClient()
				                                   .get()
				                                   .uri(USERS_SERVICES_ENDPOINT)
				                                   .retrieve()
				                                   .bodyToMono(UsersServiceResponse.class))
				.map(UsersServiceResponse::getUsers)
				.flatMapMany(Flux::fromIterable);
	}

/*
	// TODO test. Create sample controller. Decide callback source (router function or argument of this method)
	// Returns a pair of response body DTO and translated status value for the box indicator if mapping function is specified,
	// otherwise returns response body DTO only.
	// TODO or may be use Consumer function parameter instead ?
	// TODO variant 1
	@Override
	@NonNull
	public Mono<?> execFunction(@NonNull final RouterFunction<Q, T, V> func) {    // variant 2
		// returns empty mono if function is empty
		if (!func.isExecutable())
			return Mono.empty();
		// already has checked by isExecutable but reassert here to keep inspection happy
		val device = func.getDevice();
		assert device != null;
		val clientStateMono = getClientState(device.getId());
		val responseClazz = func.getResponseClazz();
		val mapFunc = func.getMapFunction();
		if (responseClazz != null && mapFunc != null)
			return clientStateMono.flatMap(clientState -> prepareResponseSpec(clientState, func).bodyToMono(responseClazz))
                                  .map(rb -> ExecFunctionResultPair.of(rb, mapFunc.apply(rb)));
		else
			return clientStateMono.flatMap(clientState -> prepareResponseSpec(clientState, func).bodyToMono((Class<? extends RestApiDTO>)(responseClazz != null ? responseClazz : RestApiUniversalDTO.class)));
	}

	// TODO variant 2
	@Override
	@NonNull
	public Mono<? extends RestApiDTO> execFunction(@NonNull final RouterFunction<Q, T, V> func, @Nullable final Consumer<V> boxValueConsumer) { // variant 1
		// returns empty mono if function is empty
		if (!func.isExecutable())
			return Mono.empty();
		// already has checked by isExecutable but reassert here to keep inspection happy
		val device = func.getDevice();
		assert device != null;
		val clientStateMono = getClientState(device.getId());
		val responseClazz = func.getResponseClazz();
		val mapFunc = func.getMapFunction();
		if (responseClazz != null && mapFunc != null && boxValueConsumer != null)
			return clientStateMono.flatMap(clientState -> prepareResponseSpec(clientState, func).bodyToMono(responseClazz))
			                      .flatMap(rb -> Mono.just(rb).map(mapFunc).doOnNext(boxValueConsumer).map(none -> rb));
		else
			return clientStateMono.flatMap(clientState -> prepareResponseSpec(clientState, func).bodyToMono((Class<? extends RestApiDTO>)(responseClazz != null ? responseClazz : RestApiUniversalDTO.class)));
	}
*/

	// TODO variant 3
	@NonNull
//	public Mono<ExecFunctionResultPair<? extends RestApiDTO, ? extends RestApiErrorDTO, V>> execFunction(@NonNull final String func) {
	public Mono<ExecFunctionResultPair<? extends RestApiDTO, ? extends RestApiErrorDTO, ?>> execFunction(@NonNull final String func) {
//		return getFunction(func).filter(RouterFunction::isExecutable)
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
//			           Mono<ExecFunctionResultPair<? extends RestApiDTO, V>> r;
			           Mono<ExecFunctionResultPair<? extends RestApiDTO, ? extends RestApiErrorDTO, V>> r;
//			           Mono<ExecFunctionResultPair<? extends RestApiDTO, ? extends RestApiErrorDTO, ?>> r;
			           if (responseClazz != null && mapFunc != null)
				           r = clientStateMono.flatMap(clientState -> prepareResponseSpec(clientState, routerFunction).bodyToMono(responseClazz))
				                              .map(rb -> ExecFunctionResultPair.of(rb, mapFunc.apply(rb)));
			           else // breaks type safety for mapping function
				           r = clientStateMono.flatMap(clientState -> prepareResponseSpec(clientState, routerFunction).bodyToMono((Class<? extends RestApiDTO>) (responseClazz != null ? responseClazz : RestApiUniversalDTO.class)))
				                              .map(rb -> ExecFunctionResultPair.of(rb, null));
			           r = r.onErrorMap(IOException.class, e -> new CiscoServiceException("Network error", e.getLocalizedMessage(), e));
			           return errorFunc != null ? r.onErrorResume(CiscoRestApiHandledException.class, e -> Mono.just(ExecFunctionResultPair.of(e.getErrorResponse(), errorFunc.apply(e.getErrorResponse())))) : r;
		           });
	}

/*
	@Override
	@NonNull
//	public Mono<ExecFunctionResultPair<? extends RestApiDTO, ? extends RestApiErrorDTO, V>> execFunction(@NonNull Mono<RouterFunction<Q, T, E, V>> function) {
	public Mono<ExecFunctionResultPair<? extends RestApiDTO, ? extends RestApiErrorDTO, V>> execFunction(@NonNull Mono<RouterFunction<Q, T, V>> function) {
		return function.filter(RouterFunction::isExecutable)
		               .flatMap(routerFunction -> {
			               // already has checked by isExecutable but reassert here to keep inspection happy
			               val device = routerFunction.getDevice();
			               assert device != null;
			               val clientStateMono = getClientState(device.getId());
			               val responseClazz = routerFunction.getResponseClazz();
			               val mapFunc = routerFunction.getMapFunction();
			               val errorFunc = routerFunction.getResourceNotFoundFunction();
//			           Mono<ExecFunctionResultPair<? extends RestApiDTO, V>> r;
			               Mono<ExecFunctionResultPair<? extends RestApiDTO, ? extends RestApiErrorDTO, V>> r;
			               if (responseClazz != null && mapFunc != null)
				               r = clientStateMono.flatMap(clientState -> prepareResponseSpec(clientState, routerFunction).bodyToMono(responseClazz))
				                                  .map(rb -> ExecFunctionResultPair.of(rb, mapFunc.apply(rb)));
			               else // breaks type safety for mapping function
				               r = clientStateMono.flatMap(clientState -> prepareResponseSpec(clientState, routerFunction).bodyToMono((Class<? extends RestApiDTO>) (responseClazz != null ? responseClazz : RestApiUniversalDTO.class)))
				                                  .map(rb -> ExecFunctionResultPair.of(rb, null));
			               r = r.onErrorMap(IOException.class, e -> new CiscoServiceException("Network error", e.getLocalizedMessage(), e));
			               return errorFunc != null ? r.onErrorResume(CiscoRestApiHandledException.class, e -> Mono.just(ExecFunctionResultPair.of(e.getErrorResponse(), errorFunc.apply(e.getErrorResponse())))) : r;
		               });
	}
*/


	// deduplicate code in execFunction
	private static WebClient.ResponseSpec prepareResponseSpec(@NonNull final CiscoRestApiClientState clientState,
	                                                          @NonNull final RouterFunction func) {
		val requestMethod = func.getRequestMethod();
		val uriPath = func.getUriPath();
		val requestObject = func.getRequestObject();
		assert requestMethod != null && uriPath != null;
		log.trace("requestObject: {}", requestObject);
		val rbs = clientState.getWebClient()
		                     .method(requestMethod)
		                     .uri(uriPath);
		// TODO may be concrete class is required here
//		return (requestObject != null ? rbs.body(Mono.just(requestObject), RestApiDTO.class) : rbs)
//		return (requestObject != null ? rbs.syncBody(requestObject) : rbs)
		var r = (requestObject != null ? rbs.syncBody(requestObject) : rbs).retrieve();
		if (func.getResourceNotFoundFunction() != null)
			r = r.onStatus(status -> status == HttpStatus.NOT_FOUND, clientResponse -> clientResponse.bodyToMono(RestApiErrorResponse.class).flatMap(errorBody -> Mono.error(new CiscoRestApiHandledException(clientResponse.statusCode(), errorBody))));
		return r.onStatus(HttpStatus::isError, clientResponse -> clientResponse.bodyToMono(RestApiErrorResponse.class).flatMap(errorBody -> Mono.error(new CiscoRestApiException(clientResponse.statusCode(), errorBody))));
	}

	// TODO variant 3 returning pair always and accepts router function name as an argument

	// TODO on 404 errors do not translate body, it may be html, not json

	// TODO continue.


/*
	@NonNull
	@Override
//	public Mono<RouterFunction<Q, T, E, V>> getFunction(@NonNull final String func) {
	public Mono<RouterFunction<Q, T, V>> getFunction(@NonNull final String func) {
		Assert.notNull(func, "Function name must not be null");
		return Mono.justOrEmpty(routerFunctions.get(func))
				   .switchIfEmpty(Mono.defer(() -> Mono.error(new CiscoServiceException("Function \"" + func + "\" is undefined"))));
	}
*/
}
