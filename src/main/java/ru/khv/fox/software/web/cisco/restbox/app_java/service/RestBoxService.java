/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.service;

import reactor.core.publisher.Mono;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.box.*;

import java.util.Collection;

public interface RestBoxService {

	// For use in RestBoxController
	Mono<BoxControl> getBoxControl(String boxName, String secret, BoxControlType boxControlType, int boxControlId);

	Mono<BoxControl> putStatus(String boxName, String secret, BoxControlType boxControlType, int boxControlId, boolean ready, int status);

	Mono<Integer> getStatus(String boxName, BoxControlType boxControlType, int boxControlId, boolean ready);

	Mono<BoxControl> putStatus(String boxName, BoxControlType boxControlType, int boxControlId, int status);

	Mono<Integer> getStatus(String boxName, BoxControlType boxControlType, int boxControlId);

	Mono<BoxControlAction> getAction(String boxName, BoxControlType boxControlType, int boxControlId);

	Collection<Box> getConf();

	Mono<BoxControlSensor> putOnFunc(String boxName, BoxControlType boxControlType, int boxControlId, String func);

	Mono<BoxControlSensor> putOffFunc(String boxName, BoxControlType boxControlType, int boxControlId, String func);

	Mono<BoxControlIndicator> putRFunc(String boxName, BoxControlType boxControlType, int boxControlId, String func);
}
