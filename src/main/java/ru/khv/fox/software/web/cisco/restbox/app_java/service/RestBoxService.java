/*
 * Copyright (c) 2019 Boris Fox.
 * All rights reserved.
 */

package ru.khv.fox.software.web.cisco.restbox.app_java.service;

import org.springframework.lang.NonNull;
import reactor.core.publisher.Mono;
import ru.khv.fox.software.web.cisco.restbox.app_java.model.box.*;

import java.util.Collection;

public interface RestBoxService {

/*
	@NonNull
	Mono<Box> checkAccess(@NonNull final String boxName, @NonNull final String secret);
*/

	// For use in RestBoxController
	@NonNull
	Mono<BoxControl> getBoxControl(@NonNull final String boxName, @NonNull final String secret, @NonNull final BoxControlType boxControlType, final int boxControlId);

	@NonNull
	Mono<BoxControl> putStatus(@NonNull final String boxName, @NonNull final String secret, @NonNull final BoxControlType boxControlType, final int boxControlId, final boolean ready, final int status);

	@NonNull
	Mono<Integer> getStatus(@NonNull final String boxName, @NonNull final BoxControlType boxControlType, final int boxControlId, final boolean ready);

/*
	// For use in WebSpaController
	@NonNull
	Mono<BoxControl> getBoxControl(@NonNull final String boxName, @NonNull final BoxControlType boxControlType, final int boxControlId);
*/

	@NonNull
	Mono<BoxControl> putStatus(@NonNull final String boxName, @NonNull final BoxControlType boxControlType, final int boxControlId, final int status);

	@NonNull
	Mono<Integer> getStatus(@NonNull final String boxName, @NonNull final BoxControlType boxControlType, final int boxControlId);

	@NonNull
	Mono<BoxControlAction> getAction(@NonNull final String boxName, @NonNull final BoxControlType boxControlType, final int boxControlId);

	@NonNull
	Collection<Box> getConf();

	@NonNull
	Mono<BoxControlSensor> putOnFunc(@NonNull final String boxName, @NonNull final BoxControlType boxControlType, final int boxControlId, @NonNull final String func);

	@NonNull
	Mono<BoxControlSensor> putOffFunc(@NonNull final String boxName, @NonNull final BoxControlType boxControlType, final int boxControlId, @NonNull final String func);

	@NonNull
	Mono<BoxControlIndicator> putRFunc(@NonNull final String boxName, @NonNull final BoxControlType boxControlType, final int boxControlId, @NonNull final String func);
}
