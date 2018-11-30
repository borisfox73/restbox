/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.service;

import org.springframework.lang.NonNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.Router;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.cisco.AuthServiceResponse;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.cisco.HostnameServiceResponse;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.cisco.UserServiceResponse;

import java.util.Map;

public interface CiscoRestfulService {

	@NonNull
	Map<String, Router> getRouters();

/*
	@NonNull
	Mono<AuthServiceResponse> authenticate(@NonNull final String routerId);
*/

	@NonNull
	Mono<AuthServiceResponse> checkAuthToken(@NonNull final String routerId);

	@NonNull
	Mono<Void> invalidateAuthToken(@NonNull final String routerId);

	@NonNull
	Mono<HostnameServiceResponse> getHostname(@NonNull final String routerId);

	@NonNull
	Mono<UserServiceResponse> getUser(@NonNull final String routerId, @NonNull final String username);

	@NonNull
	Flux<UserServiceResponse> getUsers(@NonNull final String routerId);
}
