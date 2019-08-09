/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.khv.fox.software.web.cisco.restbox.app_java.configuration.AppProperties;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.RouterFunction;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.box.BoxControlType;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.dto.ApiResponse;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.dto.RouterFunctionResponse;
import ru.khv.fox.software.web.cisco.restbox.app_java.service.CiscoRestfulService;
import ru.khv.fox.software.web.cisco.restbox.app_java.service.RestBoxService;
import ru.khv.fox.software.web.cisco.restbox.app_java.util.RestApiException;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static ru.khv.fox.software.web.cisco.restbox.app_java.controller.RestBoxController.callAFunction;
import static ru.khv.fox.software.web.cisco.restbox.app_java.controller.RestBoxController.callRFunction;

/**
 * Endpoints for the Web Single Page application.
 * Requires JWT authentication.
 */
@Slf4j
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping(path = "/webapi", produces = MediaType.APPLICATION_JSON_VALUE)
public class WebSpaController {
	private final AppProperties appProperties;
	private final RestBoxService restBoxService;
	@NonNull
	private final Map<String, RouterFunction> routerFunctions;
	@NonNull
	private final CiscoRestfulService ciscoService;


	// ==== API for one-page web application, with jwt ====

	// ==== Web portal calls ====

	// - list labusers and their information
	@GetMapping(path = "/list/labusers")
	public Mono<ApiResponse> getLabUsers() {
		return Mono.just(appProperties.getUsers()).map(ApiResponse::new);
	}

	// list configured/active boxcontrollers, their modules and statuses
	@GetMapping(path = "/list/boxcontrollers")
	public Mono<ApiResponse> getBoxControllers() {
		return Mono.just(restBoxService.getConf()).map(ApiResponse::new);
	}

	// client asks about lights status
	@GetMapping(path = "/get/{boxName}/{boxControlType}/{boxControlId:\\d+}")
	public Mono<ApiResponse> getBoxControllerLights(@PathVariable @NotEmpty final String boxName,
	                                                @PathVariable @NotNull final BoxControlType boxControlType,
	                                                @PathVariable final int boxControlId) {
		// Return polled box control state
		return restBoxService.getStatus(boxName, boxControlType, boxControlId).map(ApiResponse::new);
	}

	// client asks about lights action (on/off)
	@GetMapping(path = "/getaction/{boxName}/{boxControlType}/{boxControlId:\\d+}")
	public Mono<ApiResponse> getBoxControllerAction(@PathVariable @NotEmpty final String boxName,
	                                                @PathVariable @NotNull final BoxControlType boxControlType,
	                                                @PathVariable final int boxControlId) {
		// Return polled box control action
		return restBoxService.getAction(boxName, boxControlType, boxControlId).map(ApiResponse::new);
	}

	// change status for box on boxcontroller
	@PutMapping(path = "/change/{boxName}/{boxControlType}/{boxControlId:\\d+}/{status:\\d+}")
	public Mono<ApiResponse> putBoxControllerLights(@PathVariable @NotEmpty final String boxName,
	                                                @PathVariable @NotNull final BoxControlType boxControlType,
	                                                @PathVariable final int boxControlId,
	                                                @PathVariable final int status) {
		// Does not directly influence router resource state
		return restBoxService.putStatus(boxName, boxControlType, boxControlId, status).thenReturn(new ApiResponse("ok"));
	}

	// - list available action functions
	@GetMapping(path = "/list/afunctions")
	public Mono<ApiResponse> getRouterAFunctions() {
		return getRouterFunctions(RouterFunction::isAction);
	}

	// - list available read functions
	@GetMapping(path = "/list/rfunctions")
	public Mono<ApiResponse> getRouterRFunctions() {
		return getRouterFunctions(RouterFunction::isRead);
	}

	private Mono<ApiResponse> getRouterFunctions(@NonNull final Predicate<RouterFunction> functionPredicate) {
		return Flux.fromIterable(routerFunctions.values())
		           .filter(functionPredicate)
		           .map(RouterFunctionResponse::from)
		           .collect(Collectors.toList())
		           .map(ApiResponse::new);
	}

	// - change onfunc for box on boxcontroller
	@PutMapping(path = "/onfunc/{boxName}/{boxControlType}/{boxControlId:\\d+}/{func}")
	public Mono<ApiResponse> changeOnFuncOnBoxController(@PathVariable @NotBlank final String boxName,
	                                                     @PathVariable @NotNull final BoxControlType boxControlType,
	                                                     @PathVariable final int boxControlId,
	                                                     @PathVariable @NotBlank final String func) {
		// Function must be a valid action function
		return findRouterFunction(func, "Action", RouterFunction::isAction)
				.flatMap(functionName -> restBoxService.putOnFunc(boxName, boxControlType, boxControlId, functionName))
				.thenReturn(new ApiResponse("ok"));
	}

	// - change offfunc for box on boxcontroller
	@PutMapping(path = "/offfunc/{boxName}/{boxControlType}/{boxControlId:\\d+}/{func}")
	public Mono<ApiResponse> changeOffFuncOnBoxController(@PathVariable @NotBlank final String boxName,
	                                                      @PathVariable @NotNull final BoxControlType boxControlType,
	                                                      @PathVariable final int boxControlId,
	                                                      @PathVariable @NotBlank final String func) {
		// Function must be a valid action function
		return findRouterFunction(func, "Action", RouterFunction::isAction)
				.flatMap(functionName -> restBoxService.putOffFunc(boxName, boxControlType, boxControlId, functionName))
				.thenReturn(new ApiResponse("ok"));
	}

	// - change rfunc for box on boxcontroller
	@PutMapping(path = "/rfunc/{boxName}/{boxControlType}/{boxControlId:\\d+}/{func}")
	public Mono<ApiResponse> changeRFuncOnBoxController(@PathVariable @NotBlank final String boxName,
	                                                    @PathVariable @NotNull final BoxControlType boxControlType,
	                                                    @PathVariable final int boxControlId,
	                                                    @PathVariable @NotBlank final String func) {
		// Function must be a valid read function
		log.debug("boxname = {}", boxName);
		log.debug("rfunc = {}", func);
		return findRouterFunction(func, "Read", RouterFunction::isRead)
				.flatMap(functionName -> restBoxService.putRFunc(boxName, boxControlType, boxControlId, functionName))
				.thenReturn(new ApiResponse("ok"));
	}

	private Mono<String> findRouterFunction(@NonNull final String func, @NonNull final String type, @NonNull final Predicate<RouterFunction> functionPredicate) {
		return Flux.fromIterable(routerFunctions.values())
		           .filter(fn -> fn.getName().equalsIgnoreCase(func))
		           .switchIfEmpty(Mono.defer(() -> Mono.error(new RestApiException("Function '" + func + "' is not found", HttpStatus.NOT_FOUND))))
		           .filter(functionPredicate)
		           .next()
		           .map(RouterFunction::getName)
		           .switchIfEmpty(Mono.defer(() -> Mono.error(new RestApiException("Function '" + func + "' is not of " + type + " type", HttpStatus.BAD_REQUEST))));
	}

	// - call to afunc (for test)
	@PutMapping(path = "/call/afunc/{func}")
	public Mono<ApiResponse> callAFunc(@PathVariable @NotBlank final String func) {
		// Function must be a valid action function
		return findRouterFunction(func, "Action", RouterFunction::isAction).transform(callAFunction(ciscoService)).thenReturn(new ApiResponse("ok"));
	}

	// - call to rfunc (for test)
	@PutMapping(path = "/call/rfunc/{func}")
	public Mono<ApiResponse> callRFunc(@PathVariable @NotBlank final String func) {
		// Function must be a valid read function
		return findRouterFunction(func, "Read", RouterFunction::isRead).transform(callRFunction(ciscoService)).thenReturn(new ApiResponse("ok"));
	}

	// - list configured/active routers
	@GetMapping(path = "/list/routers")
	public Mono<ApiResponse> getRouters() {
		return Mono.just(ciscoService.getRouters()).map(Map::values).map(ApiResponse::new);
	}

	// - force csr re-auth
	@PutMapping(path = "/csr/reauth")
	public Mono<ApiResponse> csrReauth() {
		return ciscoService.reAuthenticateAll().thenReturn(new ApiResponse("ok"));
	}
}
