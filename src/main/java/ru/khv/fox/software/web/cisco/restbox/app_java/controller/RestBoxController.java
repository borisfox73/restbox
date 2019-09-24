/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.box.BoxControl;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.box.BoxControlIndicator;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.box.BoxControlSensor;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.box.BoxControlType;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.dto.ApiResponse;
import ru.khv.fox.software.web.cisco.restbox.app_java.service.CiscoRestfulService;
import ru.khv.fox.software.web.cisco.restbox.app_java.service.CiscoServiceException;
import ru.khv.fox.software.web.cisco.restbox.app_java.service.ExecFunctionResultPair;
import ru.khv.fox.software.web.cisco.restbox.app_java.service.RestBoxService;
import ru.khv.fox.software.web.cisco.restbox.app_java.util.RestApiException;
import ru.khv.fox.software.web.cisco.restbox.app_java.util.Utilities;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.function.Function;

/**
 * Endpoints for REST Boxes.
 * Unprotected by framework security, authentication is done via request parameters.
 */
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Validated
@RestController
@RequestMapping(path = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class RestBoxController {
	CiscoRestfulService ciscoService;
	RestBoxService restBoxService;


	// ==== API for Arduino boxes, no jwt ===

	// box inquires about lights state
	@GetMapping(path = "/get/{boxName}/{secret}/{boxControlType}/{boxControlId:\\d+}")
	public Mono<ApiResponse> getBoxControlStatus(@PathVariable @NotBlank final String boxName,
	                                             @PathVariable @NotEmpty final String secret,
	                                             @PathVariable @NotNull final BoxControlType boxControlType,
	                                             @PathVariable final int boxControlId,
	                                             @RequestParam(name = "ready", defaultValue = "true") final boolean ready,
	                                             @RequestParam(name = "inline", defaultValue = "false") final boolean inline) {
		// Chain to arrange inline polling
		final Function<Mono<BoxControl>, Mono<BoxControl>> inlineChain =
				bc -> bc.ofType(BoxControlIndicator.class)
				        .map(BoxControl::getRouterFunc)
				        .transform(Utilities.getIfPresent())
				        .transform(callRFunction(ciscoService))
                        .map(ExecFunctionResultPair::getBoxValue)
                        .transform(Utilities.getIfPresent())
                        .cast(Integer.class)
                        .flatMap(boxStatus -> restBoxService.putStatus(boxName, boxControlType, boxControlId, boxStatus))
                        .doOnNext(boxControl -> log.trace("box control after {}", boxControl))
                        .onErrorMap(ClassCastException.class, e -> new RestApiException(e.getLocalizedMessage(), HttpStatus.INTERNAL_SERVER_ERROR, e, "Cisco REST API Service result type is unsupported"));

		// Background or inline resource polling depending of parameter value
		return restBoxService.getBoxControl(boxName, secret, boxControlType, boxControlId)
		                     .transform(inline ? inlineChain : c -> c)
		                     .then(restBoxService.getStatus(boxName, boxControlType, boxControlId, ready)
		                                         .map(ApiResponse::new));
	}

	// box reports sensor state change
	@PutMapping(path = "/put/{boxName}/{secret}/{boxControlType}/{boxControlId:\\d+}/{status:\\d+}")
	public Mono<ApiResponse> putBoxControlStatus(@PathVariable @NotBlank final String boxName,
	                                             @PathVariable @NotEmpty final String secret,
	                                             @PathVariable @NotNull final BoxControlType boxControlType,
	                                             @PathVariable final int boxControlId,
	                                             @PathVariable final int status) {
		return restBoxService.putStatus(boxName, secret, boxControlType, boxControlId, true, status)
		                     .ofType(BoxControlSensor.class)
		                     .doOnNext(c -> log.debug("sensor Action {}", c.getAction()))
		                     .map(BoxControl::getRouterFunc)
		                     .transform(Utilities.getIfPresent())
		                     .transform(callAFunction(ciscoService))
                             .thenReturn(new ApiResponse("ok"));
	}

	static Function<Mono<String>, Mono<ExecFunctionResultPair<?, ?, ?>>> callAFunction(final CiscoRestfulService ciscoService) {
		return funcNameMono -> funcNameMono.doOnNext(aFunction -> log.trace("exec aFunction {}", aFunction))
		                                   .flatMap(ciscoService::execFunction)
		                                   .doOnNext(resultPair -> log.trace("exec result pair {}", resultPair))
		                                   .onErrorMap(CiscoServiceException.class, e -> new RestApiException(e.getErrorMessage(), HttpStatus.BAD_GATEWAY, e, e.getReason()))
		                                   .doOnError(ResponseStatusException.class, e -> log.error("{}: {}", e.getLocalizedMessage(), e.getReason()))
		                                   .doOnError(e -> log.error("Error: {}", e.getLocalizedMessage()));

	}

	static Function<Mono<String>, Mono<ExecFunctionResultPair<?, ?, ?>>> callRFunction(final CiscoRestfulService ciscoService) {
		return funcNameMono -> funcNameMono.doOnNext(rFunction -> log.trace("exec rFunction {}", rFunction))
		                                   .flatMap(ciscoService::execFunction)
		                                   .doOnNext(resultPair -> log.trace("exec result pair {}", resultPair))
		                                   .onErrorMap(CiscoServiceException.class, e -> new RestApiException(e.getErrorMessage(), HttpStatus.BAD_GATEWAY, e, e.getReason()))
		                                   .doOnError(ResponseStatusException.class, e -> log.error("{}: {}", e.getLocalizedMessage(), e.getReason()))
		                                   .doOnError(e -> log.error("Error: {}", e.getLocalizedMessage()));

	}
}
