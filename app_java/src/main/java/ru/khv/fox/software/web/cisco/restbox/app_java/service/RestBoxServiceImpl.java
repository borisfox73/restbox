/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.box.*;
import ru.khv.fox.software.web.cisco.restbox.app_java.util.RestApiException;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
class RestBoxServiceImpl implements RestBoxService {

	private final Map<String, Box> boxes;


	RestBoxServiceImpl(final Collection<Box> boxCollection) {
		boxes = boxCollection.stream().collect(Collectors.toUnmodifiableMap(Box::getName, box -> box));
		log.trace("Box map: {}", boxes);
	}

	private Mono<Box> getByName(final String boxName) {
		return Mono.justOrEmpty(boxes.get(boxName))
		           .switchIfEmpty(Mono.defer(() -> Mono.error(new RestApiException("Box '" + boxName + "' not found", HttpStatus.NOT_FOUND))));
	}

	private Mono<Box> getByNameAndCheckAccess(final String boxName, final String secret) {
		return getByName(boxName).filter(b -> b.getSecret().equals(secret))
		                         .switchIfEmpty(Mono.defer(() -> Mono.error(new RestApiException("auth_error", HttpStatus.UNAUTHORIZED))));
	}

	@Override
	public Mono<BoxControl> getBoxControl(final String boxName, final String secret, final BoxControlType boxControlType, final int boxControlId) {
		return getByNameAndCheckAccess(boxName, secret).as(boxMono -> lookupControl(boxMono, boxControlType, boxControlId));
	}

	@Override
	public Mono<BoxControl> putStatus(final String boxName, final String secret, final BoxControlType boxControlType, final int boxControlId, final boolean ready, final int status) {
		return getByNameAndCheckAccess(boxName, secret).as(boxMono -> lookupControl(boxMono, boxControlType, boxControlId, ready)).doOnNext(control -> control.setStatus(status));
	}

	@Override
	public Mono<BoxControl> putStatus(final String boxName, final BoxControlType boxControlType, final int boxControlId, final int status) {
		return lookupControl(boxName, boxControlType, boxControlId).doOnNext(control -> control.setStatus(status));
	}

	@Override
	public Mono<Integer> getStatus(final String boxName, final BoxControlType boxControlType, final int boxControlId, final boolean ready) {
		return lookupControl(boxName, boxControlType, boxControlId, ready).map(BoxControl::getStatus).defaultIfEmpty(0);
	}

	@Override
	public Mono<Integer> getStatus(final String boxName, final BoxControlType boxControlType, final int boxControlId) {
		return lookupControl(boxName, boxControlType, boxControlId).map(BoxControl::getStatus).defaultIfEmpty(0);
	}

	@Override
	public Mono<BoxControlAction> getAction(final String boxName, final BoxControlType boxControlType, final int boxControlId) {
		return lookupControl(boxName, boxControlType, boxControlId).map(BoxControl::getAction).defaultIfEmpty(BoxControlAction.NOOP);
	}

	@Override
	public Collection<Box> getConf() {
		return boxes.values();
	}

	@Override
	public Mono<BoxControlSensor> putOnFunc(final String boxName, final BoxControlType boxControlType, final int boxControlId, final String func) {
		return lookupSensor(boxName, boxControlType, boxControlId).doOnNext(boxSensor -> boxSensor.setOnFunc(func));
	}

	@Override
	public Mono<BoxControlSensor> putOffFunc(final String boxName, final BoxControlType boxControlType, final int boxControlId, final String func) {
		return lookupSensor(boxName, boxControlType, boxControlId).doOnNext(boxSensor -> boxSensor.setOffFunc(func));
	}

	@Override
	public Mono<BoxControlIndicator> putRFunc(final String boxName, final BoxControlType boxControlType, final int boxControlId, final String func) {
		return lookupIndicator(boxName, boxControlType, boxControlId).doOnNext(boxSensor -> boxSensor.setRFunc(func));
	}

	private Mono<BoxControl> getBoxControlMonoByTypeAndId(final Box box, final BoxControlType boxControlType, final int boxControlId) {
		return Mono.justOrEmpty(box.getControlByTypeAndId(boxControlType, boxControlId));
	}

	// non-idempotent, has side effects (bumps ready counter)
	private Mono<BoxControl> lookupControl(final Mono<Box> boxMono, final BoxControlType boxControlType, final int boxControlId, @Nullable final Boolean ready) {
		return boxMono.doOnNext(box -> box.incrementReady(ready))
		              .flatMap(box -> getBoxControlMonoByTypeAndId(box, boxControlType, boxControlId))
		              .switchIfEmpty(Mono.defer(() -> Mono.error(new RestApiException("Control with type '" + boxControlType + "' and id " + boxControlId + " not found", HttpStatus.NOT_FOUND))));
	}

	private Mono<BoxControl> lookupControl(final String boxName, final BoxControlType boxControlType, final int boxControlId, final boolean ready) {
		return getByName(boxName).as(boxMono -> lookupControl(boxMono, boxControlType, boxControlId, ready));
	}

	private Mono<BoxControl> lookupControl(final String boxName, final BoxControlType boxControlType, final int boxControlId) {
		return getByName(boxName).as(boxMono -> lookupControl(boxMono, boxControlType, boxControlId, null));
	}

	// idempotent
	private Mono<BoxControl> lookupControl(final Mono<Box> boxMono, final BoxControlType boxControlType, final int boxControlId) {
		return lookupControl(boxMono, boxControlType, boxControlId, null);
	}

	private Mono<BoxControlSensor> lookupSensor(final String boxName, final BoxControlType boxControlType, final int boxControlId) {
		return lookupControl(boxName, boxControlType, boxControlId)
		                         .ofType(BoxControlSensor.class)
		                         .switchIfEmpty(Mono.defer(() -> Mono.error(new RestApiException("Sensor with type '" + boxControlType + "' and id " + boxControlId + " not found", HttpStatus.NOT_FOUND))));
	}

	private Mono<BoxControlIndicator> lookupIndicator(final String boxName, final BoxControlType boxControlType, final int boxControlId) {
		return lookupControl(boxName, boxControlType, boxControlId)
		                         .ofType(BoxControlIndicator.class)
		                         .switchIfEmpty(Mono.defer(() -> Mono.error(new RestApiException("Indicator with type '" + boxControlType + "' and id " + boxControlId + " not found", HttpStatus.NOT_FOUND))));
	}
}
