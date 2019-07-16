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
import ru.khv.fox.software.web.cisco.restbox.app_java.model.dto.CommonResponse;
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

	// TODO cleanup

//	/**
//	 * Datatype Binder for Lookup Method enum to make conversion case-insensitive.
//	 *
//	 * @param binder MVC DataBinder instance
//	 */
//	@InitBinder
//	public void initBinder(@NonNull final WebDataBinder binder) {
//		binder.registerCustomEditor(BoxControlType.class, new CaseInsensitiveConverter<>(BoxControlType.class));
//	}


	/*
# ==== API for one-page web application, with jwt ====

	# - list configured/active boxcontrollers, their modules and statuses
	@app.route('/webapi/list/boxcontrollers', methods=['GET'])
	@jwt_required()
	def get_boxctrls():
        items = box.getConf();
        return jsonify({'message': items}), 200

	# - client asks about lights status -
	@app.route('/webapi/get/<boxname>/<t>/<i>', methods=['GET'])
	@jwt_required()
	def get_lights(boxname, t, i):
        stat = box.getStatus(boxname, t, i, False)
        return jsonify({'message': stat}), 200

	# - change status for box on boxcontroller
	@app.route('/webapi/change/<boxname>/<t>/<i>/<v>', methods=['PUT'])
	@jwt_required()
	def put_lights(boxname, t, i, v):
        box.putStatus(boxname, t, i, v, False)
        return jsonify({'message': 'ok'}), 200

# ==== end of API calls from one-page web application ====
	 */

	// TODO implement web portal calls

	// ==== Web portal calls ====

	// - list labusers and their information
	@GetMapping(path = "/list/labusers")
	public Mono<CommonResponse> getLabUsers() {
		return Mono.just(appProperties.getUsers()).map(CommonResponse::new);
	}

	// list configured/active boxcontrollers, their modules and statuses
	@GetMapping(path = "/list/boxcontrollers")
	public Mono<CommonResponse> getBoxControllers() {
		return Mono.just(restBoxService.getConf()).map(CommonResponse::new);
	}

	// client asks about lights status
	@GetMapping(path = "/get/{boxName}/{boxControlType}/{boxControlId:\\d+}")
	public Mono<CommonResponse> getBoxControllerLights(@PathVariable @NotEmpty final String boxName,
	                                                   @PathVariable @NotNull final BoxControlType boxControlType,
	                                                   @PathVariable final int boxControlId) {
		// Return polled box control state
		return restBoxService.getStatus(boxName, boxControlType, boxControlId).map(CommonResponse::new);
	}

	// client asks about lights action (on/off)
	@GetMapping(path = "/getaction/{boxName}/{boxControlType}/{boxControlId:\\d+}")
	public Mono<CommonResponse> getBoxControllerAction(@PathVariable @NotEmpty final String boxName,
	                                                   @PathVariable @NotNull final BoxControlType boxControlType,
	                                                   @PathVariable final int boxControlId) {
		// Return polled box control action
		return restBoxService.getAction(boxName, boxControlType, boxControlId).map(CommonResponse::new);
	}

	// change status for box on boxcontroller
	@PutMapping(path = "/change/{boxName}/{boxControlType}/{boxControlId:\\d+}/{status:\\d+}")
	public Mono<CommonResponse> putBoxControllerLights(@PathVariable @NotEmpty final String boxName,
	                                                   @PathVariable @NotNull final BoxControlType boxControlType,
	                                                   @PathVariable final int boxControlId,
	                                                   @PathVariable final int status) {
		// Does not directly influence router resource state
		return restBoxService.putStatus(boxName, boxControlType, boxControlId, status).thenReturn(new CommonResponse("ok"));
	}

	// - list available action functions
	@GetMapping(path = "/list/afunctions")
	public Mono<CommonResponse> getRouterAFunctions() {
		return getRouterFunctions(RouterFunction::isAction);
	}

	// - list available read functions
	@GetMapping(path = "/list/rfunctions")
	public Mono<CommonResponse> getRouterRFunctions() {
		return getRouterFunctions(RouterFunction::isRead);
	}

	private Mono<CommonResponse> getRouterFunctions(@NonNull final Predicate<RouterFunction> functionPredicate) {
		return Flux.fromIterable(routerFunctions.values())
		           .filter(functionPredicate)
		           .map(RouterFunctionResponse::from)
		           .collect(Collectors.toList())
		           .map(CommonResponse::new);
	}

	// - change onfunc for box on boxcontroller
	@PutMapping(path = "/onfunc/{boxName}/{boxControlType}/{boxControlId:\\d+}/{func}")
	public Mono<CommonResponse> changeOnFuncOnBoxController(@PathVariable @NotBlank final String boxName,
	                                                        @PathVariable @NotNull final BoxControlType boxControlType,
	                                                        @PathVariable final int boxControlId,
	                                                        @PathVariable @NotBlank final String func) {
		// Function must be a valid action function
		return findRouterFunction(func, "Action", RouterFunction::isAction)
				.flatMap(functionName -> restBoxService.putOnFunc(boxName, boxControlType, boxControlId, functionName))
				.thenReturn(new CommonResponse("ok"));
	}

	// - change offfunc for box on boxcontroller
	@PutMapping(path = "/offfunc/{boxName}/{boxControlType}/{boxControlId:\\d+}/{func}")
	public Mono<CommonResponse> changeOffFuncOnBoxController(@PathVariable @NotBlank final String boxName,
	                                                         @PathVariable @NotNull final BoxControlType boxControlType,
	                                                         @PathVariable final int boxControlId,
	                                                         @PathVariable @NotBlank final String func) {
		// Function must be a valid action function
		return findRouterFunction(func, "Action", RouterFunction::isAction)
				.flatMap(functionName -> restBoxService.putOffFunc(boxName, boxControlType, boxControlId, functionName))
				.thenReturn(new CommonResponse("ok"));
	}

	// - change rfunc for box on boxcontroller
	@PutMapping(path = "/rfunc/{boxName}/{boxControlType}/{boxControlId:\\d+}/{func}")
	public Mono<CommonResponse> changeRFuncOnBoxController(@PathVariable @NotBlank final String boxName,
	                                                       @PathVariable @NotNull final BoxControlType boxControlType,
	                                                       @PathVariable final int boxControlId,
	                                                       @PathVariable @NotBlank final String func) {
		// Function must be a valid read function
		log.debug("boxname = {}", boxName);
		log.debug("rfunc = {}", func);
		return findRouterFunction(func, "Read", RouterFunction::isRead)
				.flatMap(functionName -> restBoxService.putRFunc(boxName, boxControlType, boxControlId, functionName))
				.thenReturn(new CommonResponse("ok"));
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
	public Mono<CommonResponse> callAFunc(@PathVariable @NotBlank final String func) {
		// Function must be a valid action function
		return findRouterFunction(func, "Action", RouterFunction::isAction).transform(callAFunction(ciscoService)).thenReturn(new CommonResponse("ok"));
	}

	// - call to rfunc (for test)
	@PutMapping(path = "/call/rfunc/{func}")
	public Mono<CommonResponse> callRFunc(@PathVariable @NotBlank final String func) {
		// Function must be a valid read function
		return findRouterFunction(func, "Read", RouterFunction::isRead).transform(callRFunction(ciscoService)).thenReturn(new CommonResponse("ok"));
	}

	// TODO continue
	// - list configured/active routers
}
