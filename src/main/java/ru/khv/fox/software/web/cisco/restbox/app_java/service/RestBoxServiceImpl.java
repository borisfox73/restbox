/*
 * Copyright (c) 2018 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.box.*;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

// TODO reactivize
// TODO cleanup
@Slf4j
@Service
class RestBoxServiceImpl implements RestBoxService {
	@NonNull private final Map<String, Box> boxes;


	RestBoxServiceImpl(@NonNull final Collection<Box> boxCollection) {
		boxes = boxCollection.stream().collect(Collectors.toUnmodifiableMap(Box::getName, box -> box));
		log.trace("Box map: {}", boxes);
	}

	private Optional<Box> getByName(@NonNull final String boxName) {
		return Optional.ofNullable(boxes.get(boxName));
	}

	@Override
	public boolean checkAccess(@NonNull final String boxName, @NonNull final String secret) {
		return getByName(boxName).filter(b -> b.getSecret().equals(secret)).isPresent();
	}

	@Override
	public void putStatus(@NonNull final String boxName, @NonNull final BoxControlType boxControlType, final int boxControlId, final boolean ready, final int status) {
/*
		getByName(boxName).ifPresent(box -> {
			if (ready)
				box.incrementReady();
			box.getControlByTypeAndId(boxControlType, boxControlId).ifPresent(control -> control.setStatus(status));
		});
*/
		lookupControl(boxName, boxControlType, boxControlId, ready).ifPresent(control -> control.setStatus(status));
	}

	@Override
	public int getStatus(@NonNull final String boxName, @NonNull final BoxControlType boxControlType, final int boxControlId, final boolean ready) {
/*
		return getByName(boxName).flatMap(box -> {
			if (ready)
				box.incrementReady();
			return box.getControlByTypeAndId(boxControlType, boxControlId).map(BoxControl::getStatus);
		}).orElse(0);
*/
		return lookupControl(boxName, boxControlType, boxControlId, ready).map(BoxControl::getStatus).orElse(0);
	}

	@NonNull
	@Override
	public Collection<Box> getConf() {
		return boxes.values();
	}

	@Override
	public void putOnFunc(@NonNull final String boxName, @NonNull final BoxControlType boxControlType, final int boxControlId, @NonNull final BoxControlOnOffFunctions func) {
		lookupSensor(boxName, boxControlType, boxControlId).ifPresent(boxSensor -> boxSensor.setOnFunc(func));
	}

	@Override
	public void putOffFunc(@NonNull final String boxName, @NonNull final BoxControlType boxControlType, final int boxControlId, @NonNull final BoxControlOnOffFunctions func) {
		lookupSensor(boxName, boxControlType, boxControlId).ifPresent(boxSensor -> boxSensor.setOffFunc(func));
	}

	@Override
	public void putRFunc(@NonNull final String boxName, @NonNull final BoxControlType boxControlType, final int boxControlId, @NonNull final BoxControlRFunctions func) {
		lookupIndicator(boxName, boxControlType, boxControlId).ifPresent(boxSensor -> boxSensor.setRFunc(func));
	}

	// non-idempotent, has side effects (bumps ready counter)
	private Optional<BoxControl> lookupControl(@NonNull final String boxName, @NonNull final BoxControlType boxControlType, final int boxControlId, final boolean ready) {
		return getByName(boxName).stream()
		                         .peek(box -> { if (ready) box.incrementReady(); })
		                         .findAny()
		                         .flatMap(box -> box.getControlByTypeAndId(boxControlType, boxControlId));
	}

	private Optional<BoxControlSensor> lookupSensor(@NonNull final String boxName, @NonNull final BoxControlType boxControlType, final int boxControlId) {
		return getByName(boxName).flatMap(box -> box.getControlByTypeAndId(boxControlType, boxControlId))
		                         .filter(BoxControlSensor.class::isInstance)
		                         .map(BoxControlSensor.class::cast);
	}

	private Optional<BoxControlIndicator> lookupIndicator(@NonNull final String boxName, @NonNull final BoxControlType boxControlType, final int boxControlId) {
		return getByName(boxName).flatMap(box -> box.getControlByTypeAndId(boxControlType, boxControlId))
		                         .filter(BoxControlIndicator.class::isInstance)
		                         .map(BoxControlIndicator.class::cast);
	}
}
