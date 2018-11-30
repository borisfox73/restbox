/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.service;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.Router;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.cisco.AuthServiceResponse;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.cisco.HostnameServiceResponse;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.cisco.UserServiceResponse;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.cisco.UsersServiceResponse;

import javax.net.ssl.SSLException;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CiscoRestfulServiceImpl implements CiscoRestfulService {
	private static final String HOSTNAME_SERVICES_ENDPOINT = "/global/host-name";
	private static final String USERS_SERVICES_ENDPOINT = "/global/local-users";
	private static final String USER_SERVICES_ENDPOINT = USERS_SERVICES_ENDPOINT + "/{user-name}";

	@NonNull
	private final Map<String, CiscoRestApiClientState> routers;


	CiscoRestfulServiceImpl(@NonNull final Collection<Router> routerCollection,
	                        @NonNull final WebClient.Builder webClientBuilder,
	                        @Value("#{appProperties.sslIgnoreValidation}") final boolean sslIgnoreValidation) throws SSLException {
		val clientStateFactory = CiscoRestApiClientState.getFactory(webClientBuilder, sslIgnoreValidation);
		routers = routerCollection.stream()
		                          .map(clientStateFactory::createRestApiClientState)
		                          .collect(Collectors.toUnmodifiableMap(state -> state.getRouter().getId(), state -> state));
		log.trace("Router client state map: {}", routers);
	}


	@NonNull
	@Override
	public Map<String, Router> getRouters() {
		return routers.values()
		              .stream()
		              .map(CiscoRestApiClientState::getRouter)
		              .collect(Collectors.toMap(Router::getId, router -> router));
	}

	@NonNull
	private Mono<CiscoRestApiClientState> getClientState(@NonNull final String routerId) {
		return Mono.justOrEmpty(routers.get(routerId));
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
	public Mono<HostnameServiceResponse> getHostname(@NonNull final String routerId) {
		return getClientState(routerId)
				.flatMap(clientState -> clientState.getWebClient()
				                                   .get()
				                                   .uri(HOSTNAME_SERVICES_ENDPOINT)
				                                   .retrieve()
				                                   .bodyToMono(HostnameServiceResponse.class));
	}

	@NonNull
	@Override
	public Mono<UserServiceResponse> getUser(@NonNull final String routerId, @NonNull final String username) {
		return getClientState(routerId)
				.flatMap(clientState -> clientState.getWebClient()
				                                   .get()
				                                   .uri(USER_SERVICES_ENDPOINT, username)
				                                   .retrieve()
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

	// TODO continue.
}
