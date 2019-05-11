/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.CommonResponse;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.box.BoxControlType;
import ru.khv.fox.software.web.cisco.restbox.app_java.service.RestBoxService;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * Endpoints for the Web Single Page application.
 * Requires JWT authentication.
 */
@RequiredArgsConstructor
@RestController
public class WebSpaController {
	private final RestBoxService restBoxService;

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

	// list configured/active boxcontrollers, their modules and statuses
	@GetMapping(path = "/webapi/list/boxcontrollers", produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<CommonResponse> getBoxControllers() {
/*
		val boxConf = restBoxService.getConf();
		log.trace("box configuration: {}", boxConf);
		return Mono.just(new CommonResponse(boxConf));
*/
		return Mono.just(restBoxService.getConf()).map(CommonResponse::new);
	}

	// client asks about lights status
	@GetMapping(path = "/webapi/get/{boxName}/{boxControlType}/{boxControlId:\\d+}", produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<CommonResponse> getBoxControllerLights(@PathVariable @Valid @NotEmpty final String boxName,
	                                                   @PathVariable @Valid @NotNull final BoxControlType boxControlType,
	                                                   @PathVariable final int boxControlId) {
/* TODO cleanup
		val status = restBoxService.getStatus(boxName, boxControlType, boxControlId, false);
		log.trace("GET: box {} control {} id {} status = {}", boxName, boxControlType, boxControlId, status);
		return Mono.just(new CommonResponse(status));
*/
		return restBoxService.getStatus(boxName, boxControlType, boxControlId, false).map(CommonResponse::new);
	}

	// change status for box on boxcontroller
	@PutMapping(path = "/webapi/change/{boxName}/{boxControlType}/{boxControlId:\\d+}/{status:\\d+}", produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<CommonResponse> putBoxControllerLights(@PathVariable @Valid @NotEmpty final String boxName,
	                                                   @PathVariable @Valid @NotNull final BoxControlType boxControlType,
	                                                   @PathVariable final int boxControlId,
	                                                   @PathVariable final int status) {
/*
		log.trace("PUT: box {} control {} id {} status {}", boxName, boxControlType, boxControlId, status);
		restBoxService.putStatus(boxName, boxControlType, boxControlId, false, status);
		return Mono.just(new CommonResponse("ok"));
*/
		return restBoxService.putStatus(boxName, boxControlType, boxControlId, false, status).thenReturn(new CommonResponse("ok"));
	}
}
