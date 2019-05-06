/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.CommonResponse;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.box.BoxControl;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.box.BoxControlType;
import ru.khv.fox.software.web.cisco.restbox.app_java.service.RestBoxService;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * Endpoints for REST Boxes.
 * Unprotected by framework security, authentication is done via request parameters.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
public class RestBoxController {
	private final RestBoxService restBoxService;


	// TODO cleanup
	/*
# ==== API for Arduino boxes, no jwt ===
@app.route('/api/get/<boxname>/<secret>/<t>/<i>', methods = ['GET'])
def get_box(boxname,secret,t,i):
  if box.checkAccess(boxname,secret):
    stat = box.getStatus(boxname,t,i,True)
    return jsonify( { 'message': stat } ), 200
  else:
    return jsonify( { 'message': 'auth_error'} ), 401

@app.route('/api/put/<boxname>/<secret>/<t>/<i>/<value>', methods = ['PUT'])
def put_box(boxname,secret,t,i,value):
  if box.checkAccess(boxname,secret):
    box.putStatus(boxname,t,i,value,True)
    # call action function
    valint = int(value)
    if t != 'usonic':
        if valint == 1:
            cisco.call_afunc(box.get_onfunc(boxname,t,i))
        if valint == 0:
            cisco.call_afunc(box.get_offfunc(boxname,t,i))
    else:
        if valint > 50 and valint < 100:
            cisco.call_afunc(box.get_onfunc(boxname,t,i))
        if valint < 50 and valint > 5:
            cisco.call_afunc(box.get_offfunc(boxname,t,i))
    return jsonify( { 'message': 'ok' } ), 200
  else:
    return jsonify( { 'message': 'auth_error'} ), 401
	 */

	// box inquires about lights state
	@GetMapping(path = "/api/get/{boxName}/{secret}/{boxControlType}/{boxControlId:\\d+}", produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<CommonResponse> getBoxControlStatus(@PathVariable @Valid @NotEmpty final String boxName,
	                                                @PathVariable @Valid @NotEmpty final String secret,
	                                                @PathVariable @Valid @NotNull final BoxControlType boxControlType,
	                                                @PathVariable final int boxControlId) {
		return restBoxService.checkAccess(boxName, secret)
		                     .then(restBoxService.getStatus(boxName, boxControlType, boxControlId, true).map(CommonResponse::new));
	}

	// box reports sensor state change
	@PutMapping(path = "/api/put/{boxName}/{secret}/{boxControlType}/{boxControlId:\\d+}/{status:\\d+}", produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<CommonResponse> putBoxControlStatus(@PathVariable @Valid @NotEmpty final String boxName,
	                                                @PathVariable @Valid @NotEmpty final String secret,
	                                                @PathVariable @Valid @NotNull final BoxControlType boxControlType,
	                                                @PathVariable final int boxControlId,
	                                                @PathVariable final int status) {
		return restBoxService.checkAccess(boxName, secret)
		                     .then(restBoxService.putStatus(boxName, boxControlType, boxControlId, true, status))
		                     .map(BoxControl::getAction)
		                     .doOnNext(a -> log.debug("Action = {}", a))
		                     .thenReturn(new CommonResponse("ok"));
		// TODO implement router action. How to determine to which router it's bound ?
		// boxControl.getAction(status)
	}
}
