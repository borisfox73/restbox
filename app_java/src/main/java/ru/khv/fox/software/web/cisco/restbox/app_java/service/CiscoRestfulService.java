/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.Router;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.cisco.*;

import java.util.Map;

public interface CiscoRestfulService {

	Map<String, Router> getRouters();

	Mono<AuthServiceResponse> checkAuthToken(String routerId);

	Mono<Void> invalidateAuthToken(String routerId);

	Mono<Void> reAuthenticate(String routerId);

	Mono<Void> reAuthenticateAll();

	Mono<HostnameServiceResponse> getHostname(String routerId);

	Mono<UserServiceResponse> getUser(String routerId, String username);

	Flux<UserServiceResponse> getUsers(String routerId);

	Mono<ExecFunctionResultPair<? extends RestApiDTO, ? extends RestApiErrorDTO, ?>> execFunction(String func);
}
