/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.service;

import org.springframework.lang.NonNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.Router;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.cisco.*;

import java.util.Map;

//public interface CiscoRestfulService<Q extends RestApiDTO, T extends RestApiDTO, V> {
public interface CiscoRestfulService {

	@NonNull
	Map<String, Router> getRouters();

/* TODO cleanup
	@NonNull
	Mono<AuthServiceResponse> authenticate(@NonNull final String routerId);
*/

	@NonNull
	Mono<AuthServiceResponse> checkAuthToken(@NonNull final String routerId);

	@NonNull
	Mono<Void> invalidateAuthToken(@NonNull final String routerId);

	@NonNull
	Mono<Void> reAuthenticate(@NonNull String routerId);

	@NonNull
	Mono<Void> reAuthenticateAll();

	@NonNull
	Mono<HostnameServiceResponse> getHostname(@NonNull final String routerId);

	@NonNull
	Mono<UserServiceResponse> getUser(@NonNull final String routerId, @NonNull final String username);

	@NonNull
	Flux<UserServiceResponse> getUsers(@NonNull final String routerId);

/*
	// TODO variant 1
	@NonNull
	Mono<?> execFunction(@NonNull RouterFunction<Q, T, V> func);

	// TODO variant 2
	@NonNull
	Mono<? extends RestApiDTO> execFunction(@NonNull RouterFunction<Q, T, V> func, @Nullable final Consumer<V> boxValueConsumer);
*/

	// TODO variant 3
	@NonNull
//	Mono<ExecFunctionResultPair<? extends RestApiDTO, ? extends RestApiErrorDTO, V>> execFunction(@NonNull final String func);
	Mono<ExecFunctionResultPair<? extends RestApiDTO, ? extends RestApiErrorDTO, ?>> execFunction(@NonNull final String func);

/*
	@NonNull
//	Mono<ExecFunctionResultPair<? extends RestApiDTO, ? extends RestApiErrorDTO, V>> execFunction(@NonNull final Mono<RouterFunction<Q,T,E,V>> functionMono);
	Mono<ExecFunctionResultPair<? extends RestApiDTO, ? extends RestApiErrorDTO, V>> execFunction(@NonNull final Mono<RouterFunction<Q,T,V>> functionMono);
*/

	// TODO may be refactor to RouterFunction instance instead of string name
	// TODO implement method for function existence and type checking (ACTION/READ) ?
/*
	// now execFunction just return an empty stream on non-existing names.
	// Type checking is redundant because action functions can return responses too (so action implied read).
	@NonNull
//	Mono<RouterFunction<Q,T,E,V>> getFunction(@NonNull final String func);
	Mono<RouterFunction<Q,T,V>> getFunction(@NonNull final String func);
*/
}
