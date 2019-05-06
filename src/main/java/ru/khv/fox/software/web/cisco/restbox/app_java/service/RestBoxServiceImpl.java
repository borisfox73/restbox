/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
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
	@NonNull private final Map<String, Box> boxes;


	RestBoxServiceImpl(@NonNull final Collection<Box> boxCollection) {
		boxes = boxCollection.stream().collect(Collectors.toUnmodifiableMap(Box::getName, box -> box));
		log.trace("Box map: {}", boxes);
	}

	private Mono<Box> getByName(@NonNull final String boxName) {
		return Mono.justOrEmpty(boxes.get(boxName))
		           .switchIfEmpty(Mono.defer(() -> Mono.error(new RestApiException("Box '" + boxName + "' not found", HttpStatus.NOT_FOUND))));
	}

	@Override
	public Mono<Box> checkAccess(@NonNull final String boxName, @NonNull final String secret) {
		return getByName(boxName).filter(b -> b.getSecret().equals(secret))
		                         .switchIfEmpty(Mono.defer(() -> Mono.error(new RestApiException("auth_error", HttpStatus.UNAUTHORIZED))));
	}

	@Override
	public Mono<BoxControl> putStatus(@NonNull final String boxName, @NonNull final BoxControlType boxControlType, final int boxControlId, final boolean ready, final int status) {
		return lookupControl(boxName, boxControlType, boxControlId, ready).doOnNext(control -> control.setStatus(status));
	}

	@Override
	public Mono<Integer> getStatus(@NonNull final String boxName, @NonNull final BoxControlType boxControlType, final int boxControlId, final boolean ready) {
		return lookupControl(boxName, boxControlType, boxControlId, ready).map(BoxControl::getStatus).defaultIfEmpty(0);
	}

	@NonNull
	@Override
	public Collection<Box> getConf() {
		return boxes.values();
	}

	// TODO is these methods still needed ?
	@Override
	public Mono<Void> putOnFunc(@NonNull final String boxName, @NonNull final BoxControlType boxControlType, final int boxControlId, @NonNull final String func) {
		return lookupSensor(boxName, boxControlType, boxControlId).doOnNext(boxSensor -> boxSensor.setOnFunc(func)).then();
	}

	@Override
	public Mono<Void> putOffFunc(@NonNull final String boxName, @NonNull final BoxControlType boxControlType, final int boxControlId, @NonNull final String func) {
		return lookupSensor(boxName, boxControlType, boxControlId).doOnNext(boxSensor -> boxSensor.setOffFunc(func)).then();
	}

	@Override
	public Mono<Void> putRFunc(@NonNull final String boxName, @NonNull final BoxControlType boxControlType, final int boxControlId, @NonNull final String func) {
		return lookupIndicator(boxName, boxControlType, boxControlId).doOnNext(boxSensor -> boxSensor.setRFunc(func)).then();
	}


	private Mono<BoxControl> getBoxControlMonoByTypeAndId(final Box box, @NonNull final BoxControlType boxControlType, final int boxControlId) {
		return Mono.justOrEmpty(box.getControlByTypeAndId(boxControlType, boxControlId));
	}

	// non-idempotent, has side effects (bumps ready counter)
	private Mono<BoxControl> lookupControl(@NonNull final Mono<Box> boxMono, @NonNull final BoxControlType boxControlType, final int boxControlId, final boolean ready) {
		return boxMono.doOnNext(box -> box.incrementReady(ready))
		              .flatMap(box -> getBoxControlMonoByTypeAndId(box, boxControlType, boxControlId))
		              .switchIfEmpty(Mono.defer(() -> Mono.error(new RestApiException("Control with type '" + boxControlType + "' and id " + boxControlId + " not found", HttpStatus.NOT_FOUND))));
	}

	private Mono<BoxControl> lookupControl(@NonNull final String boxName, @NonNull final BoxControlType boxControlType, final int boxControlId, final boolean ready) {
		return lookupControl(getByName(boxName), boxControlType, boxControlId, ready);
	}

	private Mono<BoxControlSensor> lookupSensor(@NonNull final String boxName, @NonNull final BoxControlType boxControlType, final int boxControlId) {
		return getByName(boxName).flatMap(box -> getBoxControlMonoByTypeAndId(box, boxControlType, boxControlId))
		                         .ofType(BoxControlSensor.class)
		                         .switchIfEmpty(Mono.defer(() -> Mono.error(new RestApiException("Sensor with type '" + boxControlType + "' and id " + boxControlId + " not found", HttpStatus.NOT_FOUND))));
	}

	private Mono<BoxControlIndicator> lookupIndicator(@NonNull final String boxName, @NonNull final BoxControlType boxControlType, final int boxControlId) {
		return getByName(boxName).flatMap(box -> getBoxControlMonoByTypeAndId(box, boxControlType, boxControlId))
		                         .ofType(BoxControlIndicator.class)
		                         .switchIfEmpty(Mono.defer(() -> Mono.error(new RestApiException("Indicator with type '" + boxControlType + "' and id " + boxControlId + " not found", HttpStatus.NOT_FOUND))));
	}
}
